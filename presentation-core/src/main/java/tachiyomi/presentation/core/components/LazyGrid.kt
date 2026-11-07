package tachiyomi.presentation.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.util.miuixScroll

@Composable
fun FastScrollLazyVerticalGrid(
    columns: GridCells,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    thumbAllowed: () -> Boolean = { true },
    thumbColor: Color = Color.Unspecified,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    topContentPadding: Dp = contentPadding.calculateTopPadding(),
    bottomContentPadding: Dp = contentPadding.calculateBottomPadding(),
    endContentPadding: Dp = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit,
) {
    VerticalGridFastScroller(
        state = state,
        columns = columns,
        arrangement = horizontalArrangement,
        contentPadding = contentPadding,
        modifier = modifier,
        thumbAllowed = { userScrollEnabled && thumbAllowed() },
        thumbColor = thumbColor,
        topContentPadding = topContentPadding,
        bottomContentPadding = bottomContentPadding,
        endContentPadding = endContentPadding,
        reverseLayout = reverseLayout,
    ) {
        LazyVerticalGrid(
            modifier = Modifier.miuixScroll(userScrollEnabled),
            columns = columns,
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            userScrollEnabled = userScrollEnabled,
            overscrollEffect = null,
            content = content,
        )
    }
}
