package mihon.core.designsystem.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.miuix.LocalContainerSize

@Composable
@ReadOnlyComposable
fun isMediumWidthWindow(): Boolean {
    val size = LocalContainerSize.current ?: LocalWindowInfo.current.containerSize
    val width = with(LocalDensity.current) { size.width.toDp() }
    return width > MediumWidthWindowSize
}

@Composable
@ReadOnlyComposable
fun isExpandedWidthWindow(): Boolean {
    val size = LocalContainerSize.current ?: LocalWindowInfo.current.containerSize
    val width = with(LocalDensity.current) { size.width.toDp() }
    return width > ExpandedWidthWindowSize
}

val MediumWidthWindowSize = 600.dp
val ExpandedWidthWindowSize = 840.dp
