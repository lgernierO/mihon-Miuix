package eu.kanade.tachiyomi.ui.reader.viewer.webgpu

import androidx.compose.ui.geometry.Rect
import androidx.webgpu.AddressMode
import androidx.webgpu.ErrorFilter
import androidx.webgpu.FilterMode
import androidx.webgpu.GPUBindGroup
import androidx.webgpu.GPUBindGroupDescriptor
import androidx.webgpu.GPUBindGroupEntry
import androidx.webgpu.GPUCommandEncoder
import androidx.webgpu.GPURequestCallback
import androidx.webgpu.GPURenderPipeline
import androidx.webgpu.GPUSampler
import androidx.webgpu.GPUSamplerDescriptor
import androidx.webgpu.GPUTextureView
import ca.mpreg.webgpuviewer.filter.Filter
import ca.mpreg.webgpuviewer.filter.FullscreenFilterChain
import ca.mpreg.webgpuviewer.renderer.Fullscreen
import ca.mpreg.webgpuviewer.renderer.Hdr
import ca.mpreg.webgpuviewer.renderer.WebGpuRenderer
import androidx.webgpu.ErrorType
import androidx.webgpu.GPURenderPassEncoder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil

/**
 * Blurs the reader's top band inside the frame the WebGPU viewer is drawing right now.
 *
 * A CPU copy of the surface (PixelCopy/`getBitmap`) can land on a different frame than the one on
 * screen, so a chrome drawn from it slides over the page out of sync. Running as a [Filter] puts the
 * band and the page in one command buffer instead: the band is [FullscreenFilterChain]'s `src` for
 * this frame, so there is nothing to fall out of date.
 *
 * Passes, all on the render thread and all sized to the band rather than the screen:
 * 1. downsample the band (plus a blur-radius margin) by a power of two until one working texel is
 *    about `radius / 5`, so the blur samples neighbouring texels instead of stepping across the
 *    screen - a few large taps would smear several copies of the page into a ghost.
 * 2. a separable gaussian of thirteen taps, horizontal then vertical, at that working size.
 * 3. composite over the untouched full-resolution frame: pixels outside the band are a plain
 *    `textureLoad` of the original, and inside it the blurred result is mixed in with a weight
 *    that fades to zero at the band's bottom edge, so no seam shows.
 *
 * [update] is called from the main thread; everything that touches a GPU object runs in [run].
 * A fatal failure disables the filter (and reports through [failed]) instead of drawing a black
 * frame, after which the chain runs its plain path.
 */
internal class ReaderChromeBlurFilter : Filter() {

    /** Chrome geometry in surface pixels. [Rect.top] may be negative: the bar starts off-screen. */
    private data class Band(
        val bounds: Rect,
        val alpha: Float,
        val radiusPx: Float,
    )

    /** Everything one frame needs, read once so a concurrent [update] cannot half-apply. */
    private class Frame(
        val bandLeft: Float,
        val bandTop: Float,
        val bandWidth: Float,
        val bandHeight: Float,
        val trackedLeft: Float,
        val trackedTop: Float,
        val trackedWidth: Float,
        val trackedHeight: Float,
        val sourceRect: FloatArray,
        val alpha: Float,
        val feather: Float,
        val levels: Int,
        val sigma: Float,
        val levelWidths: IntArray,
        val levelHeights: IntArray,
    )

    private class Pipelines(
        val downsample: GPURenderPipeline,
        val blur: GPURenderPipeline,
        val composite: GPURenderPipeline,
    )

    /** A bind group kept while the same views come back - the chain rotates its scratch views. */
    private class CachedGroup(
        val pipeline: GPURenderPipeline,
        val first: Long,
        val second: Long,
        val group: GPUBindGroup,
    )

    @Volatile
    private var band: Band? = null

    private val failedState = MutableStateFlow(false)

    /** True once this filter gave up for good; the chain then skips it entirely. */
    val failed: StateFlow<Boolean> = failedState.asStateFlow()

    private var loggedFailure = false

    /** One entry per colour format: an HDR toggle swaps it and this rebuilds once. */
    private val blurPipelineCache = HashMap<Int, Pipelines>(2)

    /** Kept apart from [blurPipelineCache], so a broken blur shader cannot break the fallback. */
    private val passthroughPipelines = HashMap<Int, GPURenderPipeline>(2)

    private val cachedGroups = ArrayList<CachedGroup>(12)

    /** Error-scope results arrive on the driver's callback thread; run them where they are. */
    private val callbackExecutor = Executor { it.run() }

    private val scratchViews = ArrayList<GPUTextureView>(8)

    private var sampler: GPUSampler? = null

    private val downsampleBytes = ByteBuffer.allocateDirect(DOWNSAMPLE_BYTES).order(ByteOrder.nativeOrder())
    private val blurBytes = ByteBuffer.allocateDirect(BLUR_BYTES).order(ByteOrder.nativeOrder())
    private val compositeBytes = ByteBuffer.allocateDirect(COMPOSITE_BYTES).order(ByteOrder.nativeOrder())

    override val label: String get() = "ReaderChromeBlur"

    /** No work at all while the chrome is hidden: the chain then draws the swapchain directly. */
    override val active: Boolean
        get() = enabled && !failedState.value && band != null

    /**
     * Chrome geometry for the next frame, in surface pixels.
     *
     * @param bounds top band rectangle; `null` or an empty rectangle turns the filter off.
     * @param alpha visible strength of the band, `0..1`.
     * @param radiusPx blur radius in surface pixels, at most [MAX_RADIUS_PX].
     */
    fun update(bounds: Rect?, alpha: Float, radiusPx: Float) {
        val next = bounds
            ?.takeIf { it.width > 0f && it.height > 0f && alpha > 0.001f && alpha.isFinite() }
            ?.let {
                Band(
                    bounds = it,
                    alpha = alpha.coerceIn(0f, 1f),
                    radiusPx = radiusPx.takeIf(Float::isFinite)?.coerceIn(0f, MAX_RADIUS_PX) ?: 0f,
                )
            }
        if (next == band) return
        band = next
        invalidate()
    }

    override fun run(
        chain: FullscreenFilterChain,
        encoder: GPUCommandEncoder,
        src: GPUTextureView,
        srcWidth: Int,
        srcHeight: Int,
        dst: GPUTextureView,
        dstWidth: Int,
        dstHeight: Int,
    ) {
        val band = band
        val frame = if (band == null || failedState.value) {
            null
        } else {
            frameFor(band, srcWidth, dstWidth, dstHeight)
        }
        try {
            if (frame == null) {
                // Nothing to blur, but dst still has to be written or the frame shows garbage.
                passthroughSafely(encoder, src, dst)
            } else {
                blurFrame(chain, encoder, src, dst, frame)
            }
        } catch (t: Throwable) {
            // A broken pipeline is permanent; fall back to the untouched frame for this one.
            fail(t)
            passthroughSafely(encoder, src, dst)
        } finally {
            releaseScratch(chain)
        }
    }

    private fun blurFrame(
        chain: FullscreenFilterChain,
        encoder: GPUCommandEncoder,
        src: GPUTextureView,
        dst: GPUTextureView,
        frame: Frame,
    ) {
        val pipes = blurPipelines()
        val linearSampler = sampler()

        // 1. Downsample into progressively smaller working textures. The first pass crops the
        // tracked band out of the full frame, later ones read the whole previous level.
        var source = src
        var sourceRect = frame.sourceRect
        for (level in 1..frame.levels) {
            val targetWidth = frame.levelWidths[level - 1]
            val targetHeight = frame.levelHeights[level - 1]
            val target = chain.scratch(targetWidth, targetHeight).also { scratchViews.add(it) }
            val group = bindGroup(
                pipes.downsample,
                source,
                0L,
            ) {
                arrayOf(
                    GPUBindGroupEntry(0, textureView = source),
                    GPUBindGroupEntry(1, sampler = linearSampler),
                )
            }
            val bytes = downsampleBytes.apply {
                clear()
                putFloat(sourceRect[0])
                putFloat(sourceRect[1])
                putFloat(sourceRect[2])
                putFloat(sourceRect[3])
                flip()
            }
            val pass = Fullscreen.beginPass(encoder, target, "$label downsample $level")
            try {
                pass.setPipeline(pipes.downsample)
                pass.setImmediates(0, bytes)
                pass.setBindGroup(0, group)
                pass.draw(3)
            } finally {
                pass.finishReaderPass()
            }
            source = target
            // Every later level covers the whole previous one.
            sourceRect = FULL_RECT
        }

        // 2. Separable gaussian at the working size: one texel of the working texture is
        // `2^levels` pixels of the band, so thirteen taps reach about three sigma.
        var blurred = source
        val workingWidth = frame.levelWidths[frame.levels - 1]
        val workingHeight = frame.levelHeights[frame.levels - 1]
        for (direction in 0..1) {
            val target = chain.scratch(workingWidth, workingHeight).also { scratchViews.add(it) }
            val group = bindGroup(pipes.blur, blurred, 0L) {
                arrayOf(
                    GPUBindGroupEntry(0, textureView = blurred),
                    GPUBindGroupEntry(1, sampler = linearSampler),
                )
            }
            val bytes = blurBytes.apply {
                clear()
                putFloat(if (direction == 0) 1f else 0f)
                putFloat(if (direction == 0) 0f else 1f)
                putFloat(frame.sigma)
                putFloat(0f)
                flip()
            }
            val pass = Fullscreen.beginPass(encoder, target, "$label blur $direction")
            try {
                pass.setPipeline(pipes.blur)
                pass.setImmediates(0, bytes)
                pass.setBindGroup(0, group)
                pass.draw(3)
            } finally {
                pass.finishReaderPass()
            }
            blurred = target
        }

        // 3. Composite: the frame exactly as it was outside the band, the blurred band inside it.
        val compositeGroup = bindGroup(pipes.composite, src, blurred.handle) {
            arrayOf(
                GPUBindGroupEntry(0, textureView = src),
                GPUBindGroupEntry(1, textureView = blurred),
                GPUBindGroupEntry(2, sampler = linearSampler),
            )
        }
        val bytes = compositeBytes.apply {
            clear()
            putFloat(frame.bandLeft)
            putFloat(frame.bandTop)
            putFloat(frame.bandWidth)
            putFloat(frame.bandHeight)
            putFloat(frame.trackedLeft)
            putFloat(frame.trackedTop)
            putFloat(frame.trackedWidth)
            putFloat(frame.trackedHeight)
            putFloat(frame.feather)
            putFloat(frame.alpha)
            putFloat(0f)
            putFloat(0f)
            flip()
        }
        val pass = Fullscreen.beginPass(encoder, dst, "$label composite")
        try {
            pass.setPipeline(pipes.composite)
            pass.setImmediates(0, bytes)
            pass.setBindGroup(0, compositeGroup)
            pass.draw(3)
        } finally {
            pass.finishReaderPass()
        }
    }

    /**
     * Writes the frame through untouched. Used when there is no band and after a failure, so a
     * frame is never left blank - which is what a black band would look like.
     */
    private fun passthroughSafely(
        encoder: GPUCommandEncoder,
        src: GPUTextureView,
        dst: GPUTextureView,
    ) {
        try {
            val pipeline = passthroughPipeline()
            val group = bindGroup(pipeline, src, 0L) {
                arrayOf(GPUBindGroupEntry(0, textureView = src))
            }
            val pass = Fullscreen.beginPass(encoder, dst, "$label pass through")
            try {
                pass.setPipeline(pipeline)
                pass.setBindGroup(0, group)
                pass.draw(3)
            } finally {
                pass.finishReaderPass()
            }
        } catch (t: Throwable) {
            // Cannot even copy: drop this frame rather than leave the cleared texture behind.
            fail(t)
            throw t
        }
    }

    private fun fail(t: Throwable) {
        if (!loggedFailure) {
            loggedFailure = true
            logcat(LogPriority.ERROR, t) { "$label disabled after a fatal error" }
        }
        failedState.value = true
        // Guard for good: the chain skips an inactive filter.
        enabled = false
    }

    /** The same, for a failure the driver only reports asynchronously. */
    private fun failAsync(message: String) {
        if (!loggedFailure) {
            loggedFailure = true
            logcat(LogPriority.ERROR) { message }
        }
        failedState.value = true
        enabled = false
    }

    private fun blurPipelines(): Pipelines = blurPipelineCache.getOrPut(Hdr.frameFormat) { buildBlurPipelines() }

    /**
     * Builds the three blur pipelines. Anything already built is released if a later one throws,
     * so a half-built set never leaks.
     */
    private fun buildBlurPipelines(): Pipelines {
        val format = Hdr.frameFormat
        var downsample: GPURenderPipeline? = null
        var blur: GPURenderPipeline? = null
        var composite: GPURenderPipeline? = null
        try {
            downsample = Fullscreen.buildPipeline(DOWNSAMPLE_FS, format, "$label downsample")
            blur = Fullscreen.buildPipeline(BLUR_FS, format, "$label blur")
            composite = Fullscreen.buildPipeline(COMPOSITE_FS, format, "$label composite")
            return Pipelines(downsample, blur, composite)
        } catch (t: Throwable) {
            downsample?.close()
            blur?.close()
            composite?.close()
            throw t
        }
    }

    /** Its own cache and shader: a broken blur pipeline must not take the fallback down with it. */
    private fun passthroughPipeline(): GPURenderPipeline = passthroughPipelines.getOrPut(Hdr.frameFormat) {
        Fullscreen.buildPipeline(PASSTHROUGH_FS, Hdr.frameFormat, "$label pass through")
    }

    /**
     * Builds every pipeline on the render thread ahead of the first visible frame, with GPU error
     * scopes around it, and reports whether the driver accepted them.
     *
     * Call this before enabling the filter: a shader the driver rejects is then known before any
     * frame is drawn, so the reader never shows one frame with the band left unwritten. The
     * scopes are pushed and popped inside one render-thread block - no suspension between them -
     * and their results are awaited afterwards, outside any scope. The host leaves its readable
     * fallback in place until the driver confirms success.
     */
    suspend fun prepare(): Boolean {
        if (failedState.value) return false
        val outstanding = AtomicInteger(2)
        val failures = AtomicInteger(0)
        val settled = CompletableDeferred<Unit>()
        try {
            WebGpuRenderer.withContext {
                val gpu = device
                gpu.pushErrorScope(ErrorFilter.Validation)
                gpu.pushErrorScope(ErrorFilter.OutOfMemory)
                try {
                    blurPipelines()
                    passthroughPipeline()
                } finally {
                    repeat(2) {
                        gpu.popErrorScope(
                            callbackExecutor,
                            object : GPURequestCallback<Int> {
                                override fun onResult(result: Int) {
                                    if (result != ErrorType.NoError) failures.incrementAndGet()
                                    if (outstanding.decrementAndGet() == 0) settled.complete(Unit)
                                }

                                override fun onError(error: Exception) {
                                    failures.incrementAndGet()
                                    if (outstanding.decrementAndGet() == 0) settled.complete(Unit)
                                }
                            },
                        )
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (t: Throwable) {
            fail(t)
            return false
        }
        // WebGPU delivers these callbacks only while instance events are processed. Pump outside
        // the error scopes, so another frame's validation cannot be mistaken for this warm-up.
        val confirmed = withTimeoutOrNull(PREPARE_TIMEOUT_MS) {
            while (!settled.isCompleted) {
                WebGpuRenderer.withContext { WebGpuRenderer.instance.processEvents() }
                delay(1)
            }
            settled.await()
            true
        } ?: false
        if (!confirmed || failures.get() > 0) {
            failAsync("$label warm-up was not accepted by the driver")
            return false
        }
        return true
    }

    private fun sampler(): GPUSampler = sampler ?: device.createSampler(
        GPUSamplerDescriptor(
            label = label,
            addressModeU = AddressMode.ClampToEdge,
            addressModeV = AddressMode.ClampToEdge,
            addressModeW = AddressMode.ClampToEdge,
            magFilter = FilterMode.Linear,
            minFilter = FilterMode.Linear,
        ),
    ).also { sampler = it }

    private fun bindGroup(
        pipeline: GPURenderPipeline,
        first: GPUTextureView,
        secondHandle: Long,
        entries: () -> Array<GPUBindGroupEntry>,
    ): GPUBindGroup {
        val firstHandle = first.handle
        for (cached in cachedGroups) {
            if (cached.pipeline === pipeline && cached.first == firstHandle && cached.second == secondHandle) {
                return cached.group
            }
        }
        val layout = pipeline.getBindGroupLayout(0)
        val group = try {
            device.createBindGroup(
                GPUBindGroupDescriptor(
                    layout = layout,
                    label = label,
                    entries = entries(),
                ),
            )
        } finally {
            layout.close()
        }
        if (cachedGroups.size >= CACHED_GROUPS) {
            // Recorded passes keep their own reference, so dropping ours is safe here.
            cachedGroups.removeAt(0).group.close()
        }
        cachedGroups.add(CachedGroup(pipeline, firstHandle, secondHandle, group))
        return group
    }

    private fun GPURenderPassEncoder.finishReaderPass() {
        try {
            end()
        } finally {
            close()
        }
    }

    private fun releaseScratch(chain: FullscreenFilterChain) {
        // Every pass is already recorded, so the pool may hand these out again next frame.
        for (view in scratchViews) chain.release(view)
        scratchViews.clear()
    }

    override fun cleanup() {
        cachedGroups.forEach { it.group.close() }
        cachedGroups.clear()
        // These are this filter's own objects, not the library's shared ones.
        blurPipelineCache.values.forEach { pipes ->
            pipes.downsample.close()
            pipes.blur.close()
            pipes.composite.close()
        }
        blurPipelineCache.clear()
        passthroughPipelines.values.forEach { it.close() }
        passthroughPipelines.clear()
        sampler?.close()
        sampler = null
        scratchViews.clear()
    }

    /**
     * Turns the band into per-frame numbers: the mask the shaders clip against, the larger region
     * they sample, and how far to downsample before blurring.
     *
     * The mask is the host's rectangle as given - it already includes the bar's overhang, and its
     * top may be above the surface - so it is never clamped and never extended. Only the sampled
     * region is clipped to the surface, and the fade is kept inside the mask.
     */
    private fun frameFor(
        band: Band,
        srcWidth: Int,
        dstWidth: Int,
        dstHeight: Int,
    ): Frame? {
        if (dstWidth <= 0 || dstHeight <= 0) return null
        val dstW = dstWidth.toFloat()
        val dstH = dstHeight.toFloat()
        // The band arrives in surface pixels, which is what the frame is drawn in.
        val scale = dstW / srcWidth.coerceAtLeast(1).toFloat()
        val bounds = band.bounds
        val left = bounds.left * scale
        val top = bounds.top * scale
        val bandWidth = bounds.width * scale
        val bandHeight = bounds.height * scale
        if (bandWidth < 1f || bandHeight < 1f) return null
        val bottom = top + bandHeight

        val radius = band.radiusPx * scale
        if (radius < MIN_RADIUS_PX) return null
        // Fade inside the mask, so strength reaches zero exactly at its bottom edge.
        val feather = (bandHeight * 0.45f).coerceAtLeast(MIN_FEATHER_PX).coerceAtMost(bandHeight)

        // Margin so the blur near the band's edges reads page pixels, not the texture's edge.
        val pad = radius * 1.5f
        val trackedLeft = (left - pad).coerceIn(0f, dstW)
        val trackedTop = (top - pad).coerceIn(0f, dstH)
        val trackedRight = (left + bandWidth + pad).coerceIn(0f, dstW)
        val trackedBottom = (bottom + pad).coerceIn(0f, dstH)
        val trackedWidth = trackedRight - trackedLeft
        val trackedHeight = trackedBottom - trackedTop
        if (trackedWidth < 2f || trackedHeight < 2f) return null

        // One working texel per `radius / 5` pixels keeps the thirteen taps at about three sigma;
        // a coarser level would step over the page and repeat it.
        val targetTexel = (radius / 5f).coerceAtLeast(2f)
        var levels = 1
        var texel = 2f
        while (levels < MAX_LEVELS && texel * 2f <= targetTexel) {
            texel *= 2f
            levels++
        }
        while (levels > 1 &&
            (trackedWidth / (1 shl levels) < MIN_WORKING_PX || trackedHeight / (1 shl levels) < MIN_WORKING_PX)
        ) {
            levels--
            texel /= 2f
        }
        val sigma = (radius / 3f) / texel
        if (sigma < MIN_SIGMA) return null

        val levelWidths = IntArray(levels)
        val levelHeights = IntArray(levels)
        var previousWidth = trackedWidth
        var previousHeight = trackedHeight
        for (i in 0 until levels) {
            previousWidth = ceil(previousWidth / 2f).coerceAtLeast(1f)
            previousHeight = ceil(previousHeight / 2f).coerceAtLeast(1f)
            levelWidths[i] = previousWidth.toInt()
            levelHeights[i] = previousHeight.toInt()
        }

        return Frame(
            bandLeft = left,
            bandTop = top,
            bandWidth = bandWidth,
            bandHeight = bandHeight,
            trackedLeft = trackedLeft,
            trackedTop = trackedTop,
            trackedWidth = trackedWidth,
            trackedHeight = trackedHeight,
            sourceRect = floatArrayOf(
                trackedLeft / dstW,
                trackedTop / dstH,
                trackedWidth / dstW,
                trackedHeight / dstH,
            ),
            alpha = band.alpha,
            feather = feather,
            levels = levels,
            sigma = sigma,
            levelWidths = levelWidths,
            levelHeights = levelHeights,
        )
    }

    private companion object {
        const val MAX_RADIUS_PX = 200f
        const val MIN_RADIUS_PX = 2f
        const val MIN_FEATHER_PX = 8f
        const val MAX_LEVELS = 5
        const val MIN_WORKING_PX = 8f
        const val MIN_SIGMA = 0.4f
        const val CACHED_GROUPS = 12

        /** How long [prepare] waits for the driver to report; silence is not a failure. */
        const val PREPARE_TIMEOUT_MS = 500L

        /** `WGPUErrorType::NoError`, re-exported as the zero of [GPURequestCallback]'s result. */

        val FULL_RECT = floatArrayOf(0f, 0f, 1f, 1f)

        // Immediates use the uniform address space layout: a struct is padded to its widest
        // member's alignment. Each size below is that padded size, and every ByteBuffer is
        // allocated to exactly it - setImmediates writes the whole capacity.
        //
        // vec4<f32> rect (normalised source rect)                        -> 16 bytes
        const val DOWNSAMPLE_BYTES = 16

        // vec2<f32> direction, f32 sigma, f32 pad (alignment 8)          -> 16 bytes
        const val BLUR_BYTES = 16

        // vec4 band, vec4 tracked, f32 feather, f32 alpha (alignment 16)  -> 48 bytes
        const val COMPOSITE_BYTES = 48

        const val DOWNSAMPLE_FS = """
struct Params {
    rect: vec4<f32>,
}

var<immediate> params: Params;
@group(0) @binding(0) var src: texture_2d<f32>;
@group(0) @binding(1) var src_sampler: sampler;

@fragment
fn fs_main(in: VertexOutput) -> @location(0) vec4<f32> {
    // Linear sampling halves the source exactly once per level; clamping keeps the band's edge
    // from wrapping the page around.
    let uv = params.rect.xy + in.uv * params.rect.zw;
    return textureSampleLevel(src, src_sampler, uv, 0.0);
}
"""

        const val BLUR_FS = """
struct Params {
    direction: vec2<f32>,
    sigma: f32,
    pad: f32,
}

var<immediate> params: Params;
@group(0) @binding(0) var src: texture_2d<f32>;
@group(0) @binding(1) var src_sampler: sampler;

const TAPS: i32 = 6;

@fragment
fn fs_main(in: VertexOutput) -> @location(0) vec4<f32> {
    let size = vec2<f32>(textureDimensions(src));
    let sigma = max(params.sigma, 0.0001);
    let falloff = -0.5 / (sigma * sigma);
    let texelStep = params.direction / size;
    var sum = vec4<f32>(0.0);
    var weight = 0.0;
    for (var i = -TAPS; i <= TAPS; i = i + 1) {
        let tapDistance = f32(i);
        let w = exp(tapDistance * tapDistance * falloff);
        sum += textureSampleLevel(src, src_sampler, in.uv + texelStep * tapDistance, 0.0) * w;
        weight += w;
    }
    // Premultiplied, as the frame arrives: a weighted sum of premultiplied colour is the blur.
    return sum / weight;
}
"""

        const val COMPOSITE_FS = """
struct Params {
    band: vec4<f32>,
    tracked: vec4<f32>,
    feather: f32,
    alpha: f32,
}

var<immediate> params: Params;
@group(0) @binding(0) var src: texture_2d<f32>;
@group(0) @binding(1) var blurred: texture_2d<f32>;
@group(0) @binding(2) var blurred_sampler: sampler;

@fragment
fn fs_main(in: VertexOutput) -> @location(0) vec4<f32> {
    // The original frame, texel for texel: the page is never resampled by this filter.
    let original = textureLoad(src, vec2<i32>(in.position.xy), 0);
    let local = in.position.xy - params.band.xy;
    if (params.alpha <= 0.0 || local.x < 0.0 || local.y < 0.0 ||
        local.x >= params.band.z || local.y >= params.band.w) {
        return original;
    }

    // The blurred texture covers the tracked band, which is larger than the band itself.
    let uv = (local + params.band.xy - params.tracked.xy) / params.tracked.zw;
    let blurredColor = textureSampleLevel(blurred, blurred_sampler, uv, 0.0);

    // Premultiplied on both sides, so transparent pixels stay transparent.
    // Full strength through the bar, easing to nothing at the mask's bottom edge - the bar's
    // own edge would otherwise cut into the page as a hard line.
    let fade = smoothstep(0.0, 1.0, clamp((params.band.w - local.y) / max(params.feather, 1.0), 0.0, 1.0));
    return mix(original, blurredColor, params.alpha * fade);
}
"""

        const val PASSTHROUGH_FS = """
@group(0) @binding(0) var src: texture_2d<f32>;

@fragment
fn fs_main(in: VertexOutput) -> @location(0) vec4<f32> {
    return textureLoad(src, vec2<i32>(in.position.xy), 0);
}
"""
    }
}
