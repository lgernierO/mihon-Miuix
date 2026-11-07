package tachiyomi.presentation.core.components

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMaxBy
import top.yukonga.miuix.kmp.basic.ScrollBarAdapter
import top.yukonga.miuix.kmp.basic.ScrollBarDefaults
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Draws the native Miuix scrollbar over a lazy list without changing its measured size. */
@OptIn(ExperimentalScrollBarApi::class)
@Composable
fun VerticalFastScroller(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    thumbAllowed: () -> Boolean = { true },
    thumbColor: Color = Color.Unspecified,
    topContentPadding: Dp = Dp.Hairline,
    bottomContentPadding: Dp = Dp.Hairline,
    endContentPadding: Dp = Dp.Hairline,
    reverseLayout: Boolean = false,
    content: @Composable () -> Unit,
) {
    FastScrollerLayout(modifier = modifier, content = content) { constraints ->
        if (thumbAllowed() && (listState.canScrollForward || listState.canScrollBackward)) {
            FastScrollerThumb(
                adapter = rememberScrollBarAdapter(listState),
                height = constraints.maxHeight,
                reverseLayout = reverseLayout,
                thumbColor = thumbColor,
                topContentPadding = topContentPadding,
                bottomContentPadding = bottomContentPadding,
                endContentPadding = endContentPadding,
            )
        }
    }
}

@OptIn(ExperimentalScrollBarApi::class)
@Composable
fun VerticalGridFastScroller(
    state: LazyGridState,
    columns: GridCells,
    arrangement: Arrangement.Horizontal,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    thumbAllowed: () -> Boolean = { true },
    thumbColor: Color = Color.Unspecified,
    topContentPadding: Dp = contentPadding.calculateTopPadding(),
    bottomContentPadding: Dp = contentPadding.calculateBottomPadding(),
    endContentPadding: Dp = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
    reverseLayout: Boolean = false,
    content: @Composable () -> Unit,
) {
    FastScrollerLayout(modifier = modifier, content = content) { constraints ->
        if (thumbAllowed() && (state.canScrollForward || state.canScrollBackward)) {
            val density = LocalDensity.current
            val direction = LocalLayoutDirection.current
            val columnCount = with(density) {
                val horizontalPadding = contentPadding.calculateStartPadding(direction).roundToPx() +
                    contentPadding.calculateEndPadding(direction).roundToPx()
                with(columns) {
                    calculateCrossAxisCellSizes(
                        availableSize = (constraints.maxWidth - horizontalPadding).coerceAtLeast(0),
                        spacing = arrangement.spacing.roundToPx(),
                    ).size.coerceAtLeast(1)
                }
            }
            // Miuix 0.9.4 infers columns from the first visible row. A full-span search
            // item therefore makes its grid adapter switch to one column while dragging.
            val adapter = remember(state, columnCount) { GridScrollBarAdapter(state, columnCount) }
            FastScrollerThumb(
                adapter = adapter,
                height = constraints.maxHeight,
                reverseLayout = reverseLayout,
                thumbColor = thumbColor,
                topContentPadding = topContentPadding,
                bottomContentPadding = bottomContentPadding,
                endContentPadding = endContentPadding,
            )
        }
    }
}

@Composable
private fun FastScrollerLayout(
    modifier: Modifier,
    content: @Composable () -> Unit,
    scrollbar: @Composable (Constraints) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val contentPlaceables = subcompose("content", content).map { it.measure(constraints) }
        val width = contentPlaceables.fastMaxBy { it.width }?.width ?: constraints.minWidth
        val height = contentPlaceables.fastMaxBy { it.height }?.height ?: constraints.minHeight
        val overlayConstraints = Constraints.fixed(width, height)
        val scrollbarPlaceables = subcompose("scrollbar") { scrollbar(overlayConstraints) }
            .map { it.measure(overlayConstraints) }
        layout(width, height) {
            contentPlaceables.fastForEach { it.place(0, 0) }
            scrollbarPlaceables.fastForEach { it.place(0, 0) }
        }
    }
}

@OptIn(ExperimentalScrollBarApi::class)
@Composable
private fun FastScrollerThumb(
    adapter: ScrollBarAdapter,
    height: Int,
    reverseLayout: Boolean,
    thumbColor: Color,
    topContentPadding: Dp,
    bottomContentPadding: Dp,
    endContentPadding: Dp,
) {
    val hasTrackTravel = with(LocalDensity.current) {
        val trackSize = (height - (topContentPadding.toPx() + bottomContentPadding.toPx()).roundToInt())
            .coerceAtLeast(0)
        trackSize > ScrollBarDefaults.ThumbMinLength.toPx()
    }
    // The native drag handler remains active when its thumb fills the track.
    // Do not intercept content when there is no usable scrollbar travel.
    if (!hasTrackTravel || adapter.contentSize <= adapter.viewportSize) return

    Box(Modifier.fillMaxSize().padding(end = endContentPadding)) {
        VerticalScrollBar(
            adapter = adapter,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            reverseLayout = reverseLayout,
            trackPadding = PaddingValues(top = topContentPadding, bottom = bottomContentPadding),
            colors = ScrollBarDefaults.scrollBarColors(thumbColor = thumbColor),
        )
    }
}

/**
 * Keeps the real column count when a library search item spans a complete row.
 * Remaining unseen rows are estimated as regular rows, as in the previous fast scroller.
 */
@OptIn(ExperimentalScrollBarApi::class)
private class GridScrollBarAdapter(
    private val state: LazyGridState,
    private val columnCount: Int,
) : ScrollBarAdapter {
    private val metrics by derivedStateOf {
        val layout = state.layoutInfo
        val items = layout.visibleItemsInfo.filter { it.row != LazyGridItemInfo.UnknownRow }
        if (items.isEmpty()) return@derivedStateOf GridScrollMetrics()

        val first = items.first()
        val last = items.last()
        val lastRow = items.takeLastWhile { it.row == last.row }
        val rowCount = last.row - first.row + 1
        val averageHeight = (
            last.offset.y + lastRow.maxOf { it.size.height } - first.offset.y -
                (rowCount - 1) * layout.mainAxisItemSpacing
            ).toDouble() / rowCount
        val totalRows = last.row + 1 + ceil(
            (layout.totalItemsCount - last.index - 1).toDouble() / columnCount,
        ).toInt()
        GridScrollMetrics(
            averageHeight = averageHeight,
            rowStride = averageHeight + layout.mainAxisItemSpacing,
            totalRows = totalRows,
            firstRow = first.row,
            firstOffset = first.offset.y,
            anchorRow = last.row,
            anchorIndex = lastRow.first().index,
        )
    }

    override val viewportSize: Double
        get() = state.layoutInfo.viewportSize.height.toDouble()

    override val contentSize: Double
        get() = with(metrics) {
            averageHeight * totalRows +
                state.layoutInfo.mainAxisItemSpacing * (totalRows - 1).coerceAtLeast(0) +
                state.layoutInfo.beforeContentPadding + state.layoutInfo.afterContentPadding
        }

    override val scrollOffset: Double
        get() = with(metrics) { (firstRow * rowStride - firstOffset).coerceAtLeast(0.0) }

    override suspend fun scrollTo(scrollOffset: Double) {
        val metrics = metrics
        val itemCount = state.layoutInfo.totalItemsCount
        if (metrics.rowStride <= 0 || itemCount == 0) return

        val target = scrollOffset.coerceIn(0.0, (contentSize - viewportSize).coerceAtLeast(0.0))
        val distance = target - this.scrollOffset
        if (abs(distance) <= viewportSize) {
            state.scrollBy(distance.toFloat())
        } else {
            val row = (target / metrics.rowStride).toInt().coerceIn(0, metrics.totalRows - 1)
            val index = if (row == 0) {
                0
            } else {
                metrics.anchorIndex + (row - metrics.anchorRow) * columnCount
            }.coerceIn(0, itemCount - 1)
            val offset = (target - row * metrics.rowStride).roundToInt().coerceAtLeast(0)
            state.scrollToItem(index, offset)
        }
    }
}

private data class GridScrollMetrics(
    val averageHeight: Double = 0.0,
    val rowStride: Double = 0.0,
    val totalRows: Int = 0,
    val firstRow: Int = 0,
    val firstOffset: Int = 0,
    val anchorRow: Int = 0,
    val anchorIndex: Int = 0,
)

object Scroller {
    const val STICKY_HEADER_KEY_PREFIX = "sticky:"
}
