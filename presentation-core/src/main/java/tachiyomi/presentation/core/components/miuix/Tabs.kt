package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.glass.GlassSegmentedTabRow
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.utils.overScrollHorizontal

/**
 * Segmented tabs keep the existing sizing and selection contract. The framework owns the track's
 * selection and press feedback; the application adds scrolling for long category lists.
 */
@Composable
fun TabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minWidth: Dp = TabRowDefaults.TabRowMinWidth,
    maxWidth: Dp = TabRowDefaults.TabRowMaxWidth,
    height: Dp = 48.dp,
) {
    if (tabs.isEmpty()) return

    val selectedIndex = selectedTabIndex.coerceIn(tabs.indices)
    val minimumTabWidth = minWidth.coerceAtLeast(0.dp)
    val maximumTabWidth = maxWidth.coerceAtLeast(minimumTabWidth)
    val backdrop = LocalMiuixBackdrop.current
    val fallbackBackdrop = rememberLayerBackdrop()
    val surfaceAlpha = if (isRuntimeShaderSupported() && backdrop != null && LocalTopBarContentScrolled.current) 1f else 0f
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    var positioned by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val spacing = 0.dp
        val viewportWidth = if (constraints.hasBoundedWidth) {
            this.maxWidth
        } else {
            maximumTabWidth * tabs.size + spacing
        }
        val preferredTabWidth = ((viewportWidth - spacing) / tabs.size)
            .coerceIn(minimumTabWidth, maximumTabWidth)
        // Short rows fill the viewport; long rows retain a usable minimum width per category.
        val rowWidth = maxOf(viewportWidth, preferredTabWidth * tabs.size + spacing)
        val tabWidth = (rowWidth - spacing) / tabs.size

        LaunchedEffect(selectedIndex, tabs.size, rowWidth, viewportWidth, density) {
            val target = with(density) {
                val selectedStart = tabWidth * selectedIndex
                (selectedStart + tabWidth / 2 - viewportWidth / 2).roundToPx()
                    .coerceIn(0, (rowWidth - viewportWidth).roundToPx().coerceAtLeast(0))
            }
            if (positioned) {
                scrollState.animateScrollTo(target)
            } else {
                scrollState.scrollTo(target)
                positioned = true
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .overScrollHorizontal(isEnabled = { scrollState.canScrollBackward || scrollState.canScrollForward })
                .horizontalScroll(scrollState, overscrollEffect = null),
        ) {
            GlassSegmentedTabRow(
                tabs = tabs,
                selectedIndex = selectedIndex,
                onSelect = onTabSelected,
                backdrop = backdrop ?: fallbackBackdrop,
                surfaceAlpha = surfaceAlpha,
                modifier = Modifier
                    .width(rowWidth)
                    .semantics { collectionInfo = CollectionInfo(rowCount = 1, columnCount = tabs.size) },
                height = height,
            )
        }
    }
}
