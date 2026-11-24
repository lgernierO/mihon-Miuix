package tachiyomi.presentation.core.components.miuix

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import top.yukonga.miuix.kmp.glass.GlassMotion
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.material.GlassMaterial

/** Fixed-height detail toolbars have no native GlassTopAppBar material context. */
internal val LocalMiuixToolbarGlassProgress = compositionLocalOf<State<Float>?> { null }

@Composable
fun ToolbarGlassVisibility(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    val progress = animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = GlassMotion.topBarButtonFloat(),
        label = "detailToolbarGlass",
    )
    CompositionLocalProvider(LocalMiuixToolbarGlassProgress provides progress, content = content)
}

@Composable
internal fun animatedToolbarButtonMaterial(): GlassMaterial? {
    val progress = LocalMiuixToolbarGlassProgress.current?.value ?: return null
    val material = GlassTopAppBarDefaults.buttonMaterial()
    // Surface alpha fades the tint, rim and shadow; blur needs its own ramp to disappear as well.
    return material.copy(blurRadius = material.blurRadius * progress.coerceIn(0f, 1f))
}
