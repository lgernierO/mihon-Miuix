package tachiyomi.presentation.core.components.miuix

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.glass.GlassMotion
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.material.GlassMaterial

/** Fixed-height detail toolbars have no native GlassTopAppBar material context. */
internal val LocalMiuixToolbarGlassProgress = compositionLocalOf<State<Float>?> { null }
private val LocalPreblurredToolbar = compositionLocalOf { false }

@Composable
fun ToolbarGlassVisibility(
    visible: Boolean,
    preblurredBackdrop: Boolean = false,
    resetKey: Any? = Unit,
    content: @Composable () -> Unit,
) {
    // Reset the material animation without recreating the toolbar's visibility transition.
    val progress = key(resetKey) {
        animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = GlassMotion.topBarButtonFloat(),
            label = "detailToolbarGlass",
        )
    }
    CompositionLocalProvider(
        LocalMiuixToolbarGlassProgress provides progress,
        LocalPreblurredToolbar provides preblurredBackdrop,
        content = content,
    )
}

@Composable
internal fun toolbarButtonFill(): Color {
    val fill = GlassTopAppBarDefaults.buttonFill()
    // WebGPU already blurs the live scene beneath these buttons. Retain that view through the
    // button's material instead of covering it with the opaque unsupported-backdrop fallback.
    return if (LocalPreblurredToolbar.current) fill.copy(alpha = 0.35f) else fill
}

@Composable
internal fun animatedToolbarButtonMaterial(): GlassMaterial? {
    val progress = LocalMiuixToolbarGlassProgress.current?.value ?: return null
    val material = GlassTopAppBarDefaults.buttonMaterial()
    // Surface alpha fades the tint, rim and shadow; blur needs its own ramp to disappear as well.
    return material.copy(blurRadius = material.blurRadius * progress.coerceIn(0f, 1f))
}
