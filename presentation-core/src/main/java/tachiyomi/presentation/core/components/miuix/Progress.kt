package tachiyomi.presentation.core.components.miuix

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.basic.ProgressIndicatorDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator as MiuixCircularProgressIndicator
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator as MiuixLinearProgressIndicator

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.DefaultCircularProgressIndicatorStrokeWidth,
    progress: (() -> Float)? = null,
    size: Dp = ProgressIndicatorDefaults.DefaultCircularProgressIndicatorSize,
) {
    MiuixCircularProgressIndicator(
        modifier = modifier,
        progress = progress?.invoke()?.coerceIn(0f, 1f),
        colors = ProgressIndicatorDefaults.progressIndicatorColors(foregroundColor = color),
        strokeWidth = strokeWidth,
        size = size,
    )
}

@Composable
fun LinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
) {
    MiuixLinearProgressIndicator(
        modifier = modifier,
        progress = progress?.invoke()?.coerceIn(0f, 1f),
    )
}
