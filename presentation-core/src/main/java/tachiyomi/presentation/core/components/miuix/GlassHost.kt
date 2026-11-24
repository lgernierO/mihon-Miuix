package tachiyomi.presentation.core.components.miuix

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Page content only: bars and tabs must never sample a layer containing themselves. */
val LocalMiuixBackdrop = compositionLocalOf<Backdrop?> { null }

/** The complete activity scene, recorded outside the root popup host and its scrims. */
val LocalMiuixPopupBackdrop = compositionLocalOf<Backdrop?> { null }

/** Home navigation overlaps the page viewport; only scroll content and floating actions avoid it. */
val LocalMiuixNavigationBarPadding = compositionLocalOf { 0.dp }

/** One activity-level popup host. Individual pages retain ownership of their bars and insets. */
@Composable
fun MiuixGlassHost(content: @Composable () -> Unit) {
    val backdrop = rememberLayerBackdrop()
    val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MiuixTheme.colorScheme.surface,
    ) {
        CompositionLocalProvider(LocalMiuixPopupBackdrop provides backdrop.takeIf { supported }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (supported) Modifier.layerBackdrop(backdrop) else Modifier)
                    .background(MiuixTheme.colorScheme.surface),
            ) {
                content()
            }
        }
    }
}
