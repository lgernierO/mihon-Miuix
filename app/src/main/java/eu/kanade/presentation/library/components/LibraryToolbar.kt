package eu.kanade.presentation.library.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.SearchToolbar
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.FlipToBack
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.Pill
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.active
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.basic.ScrollBehavior as TopAppBarScrollBehavior

@Composable
fun LibraryToolbar(
    hasActiveFilters: Boolean,
    selectedCount: Int,
    title: LibraryToolbarTitle,
    onClickUnselectAll: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onClickFilter: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickGlobalUpdate: () -> Unit,
    onClickOpenRandomManga: () -> Unit,
    searchQuery: String?,
    onSearchQueryChange: (String?) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
) = when {
    selectedCount > 0 -> LibrarySelectionToolbar(
        selectedCount = selectedCount,
        onClickUnselectAll = onClickUnselectAll,
        onClickSelectAll = onClickSelectAll,
        onClickInvertSelection = onClickInvertSelection,
    )
    else -> LibraryRegularToolbar(
        title = title,
        hasFilters = hasActiveFilters,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onClickFilter = onClickFilter,
        onClickRefresh = onClickRefresh,
        onClickGlobalUpdate = onClickGlobalUpdate,
        onClickOpenRandomManga = onClickOpenRandomManga,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun LibraryRegularToolbar(
    title: LibraryToolbarTitle,
    hasFilters: Boolean,
    searchQuery: String?,
    onSearchQueryChange: (String?) -> Unit,
    onClickFilter: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickGlobalUpdate: () -> Unit,
    onClickOpenRandomManga: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
) {
    val pillAlpha = if (isSystemInDarkTheme()) 0.12f else 0.08f
    SearchToolbar(
        title = title.numberOfManga?.let { "${title.text} ($it)" } ?: title.text,
        searchQuery = searchQuery,
        onChangeSearchQuery = onSearchQueryChange,
        actions = {
            val filterTint = if (hasFilters) MaterialTheme.colorScheme.active else LocalContentColor.current
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_filter),
                        icon = MiuixIcons.Filter,
                        iconTint = filterTint,
                        onClick = onClickFilter,
                    ),
                    AppBar.OverflowAction(
                        title = stringResource(MR.strings.action_update_library),
                        onClick = onClickGlobalUpdate,
                    ),
                    AppBar.OverflowAction(
                        title = stringResource(MR.strings.action_update_category),
                        onClick = onClickRefresh,
                    ),
                    AppBar.OverflowAction(
                        title = stringResource(MR.strings.action_open_random_manga),
                        onClick = onClickOpenRandomManga,
                    ),
                ),
            )
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun LibrarySelectionToolbar(
    selectedCount: Int,
    onClickUnselectAll: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
) {
    AppBar(
        title = selectedCount.toString(),
        actionModeActions = {
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_all),
                        icon = MiuixIcons.SelectAll,
                        onClick = onClickSelectAll,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_inverse),
                        icon = MaterialSymbols.Rounded.FlipToBack,
                        onClick = onClickInvertSelection,
                    ),
                ),
            )
        },
        actionModeCounter = selectedCount,
        onCancelActionMode = onClickUnselectAll,
    )
}

@Immutable
data class LibraryToolbarTitle(
    val text: String,
    val numberOfManga: Int? = null,
)
