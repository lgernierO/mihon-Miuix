package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.category.visualName
import tachiyomi.domain.category.model.Category
import tachiyomi.presentation.core.components.miuix.TabRow

@Composable
internal fun LibraryTabs(
    categories: List<Category>,
    selectedIndex: Int,
    getItemCountForCategory: (Category) -> Int?,
    onTabItemClick: (Int) -> Unit,
) {
    if (categories.isEmpty()) return
    TabRow(
        tabs = categories.map { category ->
            getItemCountForCategory(category)?.let { count ->
                "${category.visualName} ($count)"
            } ?: category.visualName
        },
        selectedTabIndex = selectedIndex.coerceIn(categories.indices),
        onTabSelected = onTabItemClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        maxWidth = 180.dp,
        height = 48.dp,
    )
}
