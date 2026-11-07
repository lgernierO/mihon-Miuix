package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.category.visualName
import tachiyomi.domain.category.model.Category
import tachiyomi.presentation.core.components.miuix.TabRow

@Composable
internal fun LibraryTabs(
    categories: List<Category>,
    pagerState: PagerState,
    getItemCountForCategory: (Category) -> Int?,
    onTabItemClick: (Int) -> Unit,
) {
    if (categories.isEmpty()) return
    val currentPageIndex = pagerState.currentPage.coerceIn(categories.indices)
    TabRow(
        tabs = categories.map { category ->
            getItemCountForCategory(category)?.let { count ->
                "${category.visualName} ($count)"
            } ?: category.visualName
        },
        selectedTabIndex = currentPageIndex,
        onTabSelected = onTabItemClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        maxWidth = 180.dp,
        height = 48.dp,
    )
}
