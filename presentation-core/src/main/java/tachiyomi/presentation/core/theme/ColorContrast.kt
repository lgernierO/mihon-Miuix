package tachiyomi.presentation.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

/** Retains a palette's intended content color when it is legible on its actual container. */
fun Color.contentColorWithContrast(
    preferred: Color,
    fallback: Color,
    minimumContrast: Float = 4.5f,
): Color {
    if (contrastRatio(preferred, this) >= minimumContrast) return preferred
    if (contrastRatio(fallback, this) >= minimumContrast) return fallback
    return if (contrastRatio(Color.Black, this) > contrastRatio(Color.White, this)) Color.Black else Color.White
}

private fun contrastRatio(foreground: Color, background: Color): Float {
    val foregroundLuminance = foreground.compositeOver(background).luminance()
    val backgroundLuminance = background.luminance()
    return (maxOf(foregroundLuminance, backgroundLuminance) + 0.05f) /
        (minOf(foregroundLuminance, backgroundLuminance) + 0.05f)
}

/** Keeps a neutral selected surface visible on the page and cards without using the accent hue. */
fun Color.neutralSelectionContainer(
    onSurface: Color,
    backgrounds: List<Color>,
    minimumContrast: Float = 1.18f,
): Color {
    if (backgrounds.all { contrastRatio(this, it) >= minimumContrast }) return this
    for (opacity in 1..100) {
        val candidate = onSurface.copy(alpha = opacity / 100f).compositeOver(this)
        if (
            backgrounds.all { contrastRatio(candidate, it) >= minimumContrast } &&
            contrastRatio(onSurface, candidate) >= 4.5f
        ) {
            return candidate
        }
    }
    return this
}

/** Uses the original page surface only when a card's tone obscures a native selected indicator. */
fun Color.containerForSelectedIndicator(
    indicator: Color,
    fallback: Color,
    minimumContrast: Float = 3f,
): Color = if (
    contrastRatio(indicator, this) < minimumContrast &&
    contrastRatio(indicator, fallback) >= minimumContrast
) {
    fallback
} else {
    this
}
