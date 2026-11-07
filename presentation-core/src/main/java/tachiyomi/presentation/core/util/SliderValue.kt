package tachiyomi.presentation.core.util

import kotlin.math.abs
import kotlin.math.roundToInt

/** Keeps native slider and accessibility values on the same integer progression. */
fun IntProgression.sliderValue(candidate: Float): Int {
    val minimum = minOf(first, last)
    val maximum = maxOf(first, last)
    if (minimum == maximum) return minimum
    val increment = abs(step)
    val bounded = candidate.coerceIn(minimum.toFloat(), maximum.toFloat())
    val position = ((bounded - minimum) / increment).roundToInt()
    return (minimum.toLong() + position.toLong() * increment)
        .coerceIn(minimum.toLong(), maximum.toLong())
        .toInt()
}
