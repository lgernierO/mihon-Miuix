package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import eu.kanade.core.preference.PreferenceMutableState
import eu.kanade.tachiyomi.ui.library.LibraryItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.presentation.core.components.material.PullRefresh
import kotlin.time.Duration.Companion.seconds

@Composable
fun LibraryContent(
    categories: List<Category>,
    searchQuery: String?,
    selection: Set<Long>,
    contentPadding: PaddingValues,
    pagerState: PagerState,
    hasActiveFilters: Boolean,
    onChangeCurrentPage: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onContinueReadingClicked: ((LibraryManga) -> Unit)?,
    onToggleSelection: (Category, LibraryManga) -> Unit,
    onToggleRangeSelection: (Category, LibraryManga) -> Unit,
    onRefresh: suspend () -> Boolean,
    onGlobalSearchClicked: () -> Unit,
    getDisplayMode: (Int) -> PreferenceMutableState<LibraryDisplayMode>,
    getColumnsForOrientation: (Boolean) -> PreferenceMutableState<Int>,
    getItemsForCategory: (Category) -> List<LibraryItem>,
) {
    val scope = rememberCoroutineScope()
    var isRefreshing by remember(pagerState.currentPage) { mutableStateOf(false) }

    PullRefresh(
        refreshing = isRefreshing,
        enabled = selection.isEmpty(),
        indicatorPadding = contentPadding,
        onRefresh = {
            scope.launch {
                if (!onRefresh()) return@launch
                // Fake refresh status but hide it after a second as it's a long running task
                isRefreshing = true
                delay(1.seconds)
                isRefreshing = false
            }
        },
    ) {
        LibraryPager(
            state = pagerState,
            contentPadding = contentPadding,
            hasActiveFilters = hasActiveFilters,
            selection = selection,
            searchQuery = searchQuery,
            onGlobalSearchClicked = onGlobalSearchClicked,
            getCategoryForPage = { page -> categories[page] },
            getDisplayMode = getDisplayMode,
            getColumnsForOrientation = getColumnsForOrientation,
            getItemsForCategory = getItemsForCategory,
            onClickManga = { category, manga ->
                if (selection.isNotEmpty()) {
                    onToggleSelection(category, manga)
                } else {
                    onClickManga(manga.manga.id)
                }
            },
            onLongClickManga = onToggleRangeSelection,
            onClickContinueReading = onContinueReadingClicked,
        )
    }

    LaunchedEffect(pagerState.currentPage) {
        onChangeCurrentPage(pagerState.currentPage)
    }
}
