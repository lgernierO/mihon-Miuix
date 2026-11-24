package eu.kanade.presentation.reader

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import eu.kanade.tachiyomi.ui.reader.viewer.Viewer
import eu.kanade.tachiyomi.ui.reader.viewer.webgpu.WebGpuViewer
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults

/** Keeps the toolbar's material geometry in the reader renderer, without copying page pixels. */
class ReaderGpuChrome internal constructor(private val viewer: WebGpuViewer) {
    var usesGpuBlur by mutableStateOf(false)
        private set
    private var attached = false
    private var bounds: Rect? = null
    private var opacity = 0f
    private var radius = 0f
    private var overhang = 0f

    internal fun setSurfaceScene(surfaceScene: Boolean) {
        if (usesGpuBlur == surfaceScene) return
        usesGpuBlur = surfaceScene
        update()
    }

    internal fun attach() {
        attached = true
        // The library can hold a settled image even after invalidation. Keep its frame loop
        // responsive to chrome changes until AnimatedVisibility has finished the exit.
        viewer.keepReaderChromeFramesLive(true)
        update()
    }

    internal fun detach() {
        attached = false
        bounds = null
        update()
        viewer.keepReaderChromeFramesLive(false)
    }

    internal fun updateAppearance(alpha: Float, radiusPx: Float, overhangPx: Float) {
        opacity = alpha.coerceIn(0f, 1f)
        radius = radiusPx
        overhang = overhangPx
        update()
    }

    internal fun updateBounds(value: Rect) {
        bounds = value
        update()
    }

    private fun update() {
        val band = bounds?.let { Rect(it.left, it.top, it.right, it.bottom + overhang) }
        viewer.updateReaderChrome(
            boundsInWindow = band.takeIf { attached && usesGpuBlur },
            alpha = opacity,
            radiusPx = radius,
        )
    }
}

@Composable
fun rememberReaderGpuChrome(viewer: Viewer?): ReaderGpuChrome? {
    val gpuViewer = viewer as? WebGpuViewer ?: return null
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    val chrome = remember(gpuViewer) { ReaderGpuChrome(gpuViewer) }
    val surfaceScene by gpuViewer.readerChromeUsesSurface.collectAsState()
    val prepared by gpuViewer.readerChromeBlurPrepared.collectAsState()
    val failed by gpuViewer.readerChromeBlurFailed.collectAsState()
    SideEffect { chrome.setSurfaceScene(surfaceScene && prepared && !failed) }
    DisposableEffect(chrome) {
        onDispose { chrome.detach() }
    }
    return chrome
}

/** Called inside the same AnimatedVisibility transition that moves and fades the foreground. */
@Composable
fun readerGpuTopBarModifier(chrome: ReaderGpuChrome?, alpha: Float): Modifier {
    if (chrome == null) return Modifier
    val density = LocalDensity.current
    val radius = with(density) { GlassTopAppBarDefaults.ProgressiveBlurRadius.toPx() }
    val overhang = with(density) { GlassTopAppBarDefaults.BandOverhang.toPx() }
    DisposableEffect(chrome) {
        chrome.attach()
        onDispose { chrome.detach() }
    }
    SideEffect { chrome.updateAppearance(alpha, radius, overhang) }
    return Modifier.onGloballyPositioned { coordinates ->
        chrome.updateBounds(
            Rect(
                coordinates.positionInWindow(),
                Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat()),
            ),
        )
    }
}
