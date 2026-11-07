package tachiyomi.presentation.core.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * Selection sheets wrap short content and cap long scrollable content at half the window.
 * Clamp both bounds so a caller's fill modifier cannot turn this into a forced full-screen sheet.
 */
@Composable
fun Modifier.selectionSheetHeight(): Modifier {
    val maximumHeight = LocalWindowInfo.current.containerDpSize.height * 0.5f
    return layout { measurable, constraints ->
        val maximum = minOf(constraints.maxHeight, maximumHeight.roundToPx()).coerceAtLeast(0)
        val minimum = minOf(constraints.minHeight, maximum)
        val placeable = measurable.measure(
            constraints.copy(minHeight = minimum, maxHeight = maximum),
        )
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
}
