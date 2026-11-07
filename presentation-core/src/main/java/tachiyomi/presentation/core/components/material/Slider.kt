package tachiyomi.presentation.core.components.material

import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tachiyomi.presentation.core.util.sliderValue
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.Slider as MiuixSlider

@Composable
fun Slider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: IntProgression = 0..1,
    @IntRange(from = 0) steps: Int = with(valueRange) {
        ((last - first) / step - 1).coerceAtLeast(0)
    },
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val minimum = minOf(valueRange.first, valueRange.last)
    val maximum = maxOf(valueRange.first, valueRange.last)
    MiuixSlider(
        value = value.toFloat().coerceIn(minimum.toFloat(), maximum.toFloat()),
        onValueChange = { candidate -> onValueChange(valueRange.sliderValue(candidate)) },
        modifier = modifier,
        enabled = enabled && minimum < maximum,
        valueRange = minimum.toFloat()..if (minimum < maximum) maximum.toFloat() else minimum + 1f,
        steps = steps.coerceAtLeast(0),
        reverseDirection = valueRange.step < 0,
        onValueChangeFinished = onValueChangeFinished,
        hapticEffect = SliderDefaults.SliderHapticEffect.Step,
    )
}
