package tachiyomi.presentation.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator

/** Miuix owns both determinate progress and the indeterminate animation. */
@Composable
fun CombinedCircularProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    val value = progress().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = value, label = "progress")
    CircularProgressIndicator(
        modifier = modifier,
        progress = animatedProgress.takeIf { value > 0f },
    )
}
