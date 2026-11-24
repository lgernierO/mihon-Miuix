package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Body only: bars and tabs must never sample a layer containing themselves. */
val LocalMiuixBackdrop = compositionLocalOf<Backdrop?> { null }

/** Complete page and chrome, excluding the root popup host and its scrims. */
val LocalMiuixPopupBackdrop = compositionLocalOf<Backdrop?> { null }
val LocalMiuixNavigationBarPadding = compositionLocalOf { 0.dp }
val LocalContainerSize = compositionLocalOf<IntSize?> { null }

private val NoStartInset: () -> Dp = { 0.dp }
private val FullTitleAlpha: () -> Float = { 1f }
val LocalTopBarStartInset = compositionLocalOf { NoStartInset }
val LocalTopAppBarTitleAlpha = compositionLocalOf { FullTitleAlpha }
val LocalTopAppBarCenterObstructionStart = compositionLocalOf { Dp.Unspecified }

/** Apply to foreground/content only, after backdrop recording. Read animation in layout. */
@Composable
fun Modifier.navigationRailContentPadding(): Modifier {
    val inset = LocalTopBarStartInset.current
    return layout { measurable, constraints ->
        val start = inset().roundToPx().coerceIn(0, constraints.maxWidth)
        val placeable = measurable.measure(constraints.offset(horizontal = -start))
        layout(placeable.width + start, placeable.height) { placeable.placeRelative(start, 0) }
    }
}

@Composable
fun MiuixGlassHost(content: @Composable () -> Unit) {
    val surface = MiuixTheme.colorScheme.surface.copy(alpha = 1f)
    val backdrop = rememberLayerBackdrop {
        drawRect(surface)
        drawContent()
    }
    val supported = isRuntimeShaderSupported()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalContainerSize provides IntSize(constraints.maxWidth, constraints.maxHeight)) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = surface,
            ) {
                CompositionLocalProvider(LocalMiuixPopupBackdrop provides backdrop.takeIf { supported }) {
                    Box(
                        Modifier.fillMaxSize()
                            .then(if (supported) Modifier.layerBackdrop(backdrop) else Modifier),
                    ) { content() }
                }
            }
        }
    }
}
