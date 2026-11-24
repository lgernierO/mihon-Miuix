package eu.kanade.presentation.reader

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import eu.kanade.tachiyomi.ui.reader.viewer.Viewer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported

/** A reader-only source: native page pixels are recorded without bars, popup content or scrims. */
class ReaderBackdrop internal constructor(
    val source: Backdrop?,
    val modifier: Modifier,
    val pageSource: Backdrop?,
    val chromeModifier: Modifier,
)

@Composable
fun rememberReaderBackdrop(
    viewer: Viewer?,
    enabled: Boolean,
    backgroundColor: Color,
    brightness: Int,
    overlayColor: Int?,
    overlayBlendMode: BlendMode?,
): ReaderBackdrop {
    val rootView = LocalView.current.rootView
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val recorder = remember(viewer) { ReaderPageRecorder(viewer) }
    val supported = isRuntimeShaderSupported()
    var captureEnabled by remember { mutableStateOf(enabled) }
    LaunchedEffect(enabled) {
        if (enabled) {
            captureEnabled = true
        } else {
            // Keep sampling through the reader bars' 200ms exit animation.
            delay(250)
            captureEnabled = false
        }
    }
    val drawReader: ContentDrawScope.() -> Unit = {
        drawRect(backgroundColor.copy(alpha = 1f))
        if (captureEnabled && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            recorder.drawFrame(this, backgroundColor)
            if (brightness < 0) drawRect(Color.Black.copy(alpha = abs(brightness) / 100f))
            overlayColor?.let { drawRect(Color(it), blendMode = overlayBlendMode ?: BlendMode.SrcOver) }
        }
    }
    // Native pixels are the body source; the second layer adds only Compose chrome.
    val backdrop = rememberLayerBackdrop(onDraw = drawReader)
    val pageBackdrop = rememberLayerBackdrop {
        drawReader()
        drawContent()
    }
    val popupBackdrop = remember(pageBackdrop, recorder) { ReaderDrawBackdrop(pageBackdrop, recorder) }
    val chromeBackdrop = remember(backdrop, recorder) { ReaderDrawBackdrop(backdrop, recorder) }
    LaunchedEffect(recorder, captureEnabled, supported, lifecycle, rootView) {
        if (!captureEnabled || !supported) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                withFrameNanos { }
                // Sample the displayed surface while it moves as well as when it is at rest.
                // PixelCopy suspends until completion, so copies cannot queue up behind scrolling.
                recorder.refresh(rootView)
            }
        }
    }
    val modifier = if (supported) {
        Modifier
            .drawWithContent {
                recorder.drawRevision
                drawContent()
            }
            .onGloballyPositioned {
                recorder.setBounds(it.positionInWindow(), it.size)
            }
            .then(if (captureEnabled) Modifier.layerBackdrop(backdrop) else Modifier)
    } else {
        Modifier
    }
    // Only startup or an unavailable surface uses the opaque fallback. Motion keeps live sampling.
    val available = supported && captureEnabled && recorder.ready
    val chromeModifier = if (supported && captureEnabled) {
        Modifier.drawWithContent {
            recorder.drawRevision
            drawContent()
        }.layerBackdrop(pageBackdrop)
    } else {
        Modifier
    }
    return ReaderBackdrop(
        source = chromeBackdrop.takeIf { available },
        modifier = modifier,
        pageSource = popupBackdrop.takeIf { available },
        chromeModifier = chromeModifier,
    )
}

/** Invalidate the consumer's cached blur pass as well as the source recording for each new frame. */
private class ReaderDrawBackdrop(
    private val source: Backdrop,
    private val recorder: ReaderPageRecorder,
) : Backdrop by source {
    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?,
        downscaleFactor: Float,
    ) {
        recorder.drawRevision
        with(source) { drawBackdrop(density, coordinates, layerBlock, downscaleFactor) }
    }
}

private class ReaderPageRecorder(private val viewer: Viewer?) {
    var ready by mutableStateOf(false)
        private set
    private var revision by mutableIntStateOf(0)
    val drawRevision get() = revision
    private var origin = Offset.Zero
    private var bounds = Rect.Zero
    private var nativeView: View? = null
    private var nextSurfaceSearch = 0L
    private var image: ImageBitmap? = null
    private var imageView: View? = null
    private var imageSourceSize = IntSize.Zero
    private var bufferView: View? = null
    private var bufferSourceSize = IntSize.Zero
    private val location = IntArray(2)
    private val handler = Handler(Looper.getMainLooper())
    private val buffers = arrayOfNulls<Bitmap>(3)
    private var publishedBuffer = -1
    private var imageWidth = 0
    private var imageHeight = 0
    private var surfaceCopyInFlight = false

    fun setBounds(position: Offset, size: IntSize) {
        if (bounds.size.width != size.width.toFloat() || bounds.size.height != size.height.toFloat()) {
            nextSurfaceSearch = 0L
        }
        origin = position
        bounds = Rect(position, androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat()))
    }

    suspend fun refresh(root: View) {
        if (viewer == null) return
        val ordinaryView = viewer.backdropView
        if (ordinaryView != null) {
            nativeView = ordinaryView.takeIf { it.isAttachedToWindow && it.isShown && it.width > 0 && it.height > 0 }
            ready = nativeView != null
            revision++
            return
        }
        // Cancellation does not cancel PixelCopy. Do not reuse its destination until its callback.
        if (surfaceCopyInFlight) return
        // WebGPU owns a platform SurfaceView/TextureView inside Compose. Locate that surface,
        // never the decor/Compose root as a capture source (those also contain reader controls).
        val now = SystemClock.uptimeMillis()
        if (nativeView?.let { it.isAttachedToWindow && it.isShown && it.width > 0 && it.height > 0 } != true ||
            now >= nextSurfaceSearch
        ) {
            nativeView = findSurface(root)
            nextSurfaceSearch = now + 1000L
        }
        val view = nativeView ?: run {
            clearSurfaceFrame()
            return
        }
        val factor = min(1f, min(640f / view.width, 1536f / view.height))
        val width = (view.width * factor).roundToInt().coerceAtLeast(1)
        val height = (view.height * factor).roundToInt().coerceAtLeast(1)
        val sourceSize = IntSize(view.width, view.height)
        if (width != imageWidth || height != imageHeight || bufferView !== view || bufferSourceSize != sourceSize) {
            // Do not recycle images referenced by an existing Compose display list.
            buffers.fill(null)
            publishedBuffer = -1
            imageWidth = width
            imageHeight = height
            bufferView = view
            bufferSourceSize = sourceSize
            clearSurfaceFrame()
        }
        val index = (publishedBuffer + 1) % buffers.size
        val bitmap = buffers[index] ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
            buffers[index] = it
        }
        val success = when (view) {
            is TextureView -> try {
                if (view.isAvailable) {
                    val generation = bitmap.generationId
                    view.getBitmap(bitmap)
                    // TextureView can return the destination unchanged when readback fails.
                    bitmap.generationId != generation
                } else {
                    false
                }
            } catch (_: RuntimeException) {
                // Surface destruction can race the texture read; retry on the next frame.
                false
            }
            is SurfaceView -> if (view.holder.surface.isValid) copySurface(view, bitmap) else false
            else -> false
        }
        // These pixels came from the surface, not a model pose. A gesture changing the model while
        // the copy runs is expected and must not discard every frame until the gesture ends.
        if (success && view.isAttachedToWindow && view.isShown &&
            sourceSize == IntSize(view.width, view.height)
        ) {
            // Rotate destinations so the copy never overwrites the currently published image.
            publishedBuffer = index
            image = bitmap.asImageBitmap()
            imageView = view
            imageSourceSize = sourceSize
            ready = true
            revision++
        } else if (!view.isAttachedToWindow || !view.isShown) {
            clearSurfaceFrame()
        }
    }

    fun drawFrame(scope: ContentDrawScope, backgroundColor: Color): Boolean = with(scope) {
        revision // Observe native changes in the recording draw phase, not the page composition.
        val view = nativeView ?: return false
        if (!view.isAttachedToWindow || !view.isShown || view.width <= 0 || view.height <= 0) return false
        view.getLocationInWindow(location)
        val x = location[0] - origin.x
        val y = location[1] - origin.y
        if (view is SurfaceView || view is TextureView) {
            val snapshot = image?.takeIf {
                imageView === view && imageSourceSize == IntSize(view.width, view.height)
            } ?: return false
            drawRect(backgroundColor)
            translate(x, y) {
                drawImage(snapshot, dstSize = IntSize(view.width, view.height))
            }
        } else {
            val canvas = drawContext.canvas.nativeCanvas
            if (!canvas.isHardwareAccelerated) return false
            drawRect(backgroundColor)
            val saveCount = canvas.save()
            try {
                canvas.translate(x, y)
                view.draw(canvas)
            } finally {
                canvas.restoreToCount(saveCount)
            }
        }
        true
    }

    private fun clearSurfaceFrame() {
        val hadPublishedFrame = image != null || imageView != null || ready
        image = null
        imageView = null
        imageSourceSize = IntSize.Zero
        ready = false
        if (hadPublishedFrame) revision++
    }

    private suspend fun copySurface(view: SurfaceView, bitmap: Bitmap): Boolean {
        if (surfaceCopyInFlight) return false
        val surface = view.holder.surface
        if (!surface.isValid) return false
        surfaceCopyInFlight = true
        try {
            // API 33 can block in PixelCopy.request itself. Only the Surface and destination cross
            // threads; View access, frame publication and the in-flight flag remain on the UI thread.
            return withContext(Dispatchers.Default) {
                // PixelCopy cannot be cancelled. Wait for its callback before releasing the buffer,
                // even if this effect is cancelled while the copy is running.
                suspendCoroutine { continuation ->
                    try {
                        PixelCopy.request(surface, bitmap, { result ->
                            continuation.resume(result == PixelCopy.SUCCESS)
                        }, handler)
                    } catch (_: IllegalArgumentException) {
                        continuation.resume(false)
                    }
                }
            }
        } finally {
            surfaceCopyInFlight = false
        }
    }

    private fun findSurface(root: View): View? {
        var best: View? = null
        var bestArea = 0f
        fun visit(view: View) {
            if (!view.isAttachedToWindow || !view.isShown) return
            if ((view is SurfaceView || view is TextureView) && view.width > 0 && view.height > 0) {
                view.getLocationInWindow(location)
                val rect = Rect(
                    location[0].toFloat(), location[1].toFloat(),
                    location[0] + view.width.toFloat(), location[1] + view.height.toFloat(),
                )
                val overlap = rect.intersect(bounds)
                val area = overlap.width.coerceAtLeast(0f) * overlap.height.coerceAtLeast(0f)
                if (area > bestArea) {
                    bestArea = area
                    best = view
                }
            }
            if (view is ViewGroup) for (i in 0 until view.childCount) visit(view.getChildAt(i))
        }
        if (bounds.width > 0f && bounds.height > 0f) visit(root)
        return best
    }
}
