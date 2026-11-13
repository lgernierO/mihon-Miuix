package eu.kanade.presentation.manga

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.kanade.domain.manga.model.downloadedFilter
import eu.kanade.presentation.components.TabbedDialog
import eu.kanade.presentation.components.TabbedDialogPaddings
import eu.kanade.presentation.manga.components.ScanlatorFilterPage
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Group
import tachiyomi.core.common.preference.TriState
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_cancel
import tachiyomi.i18n.action_display
import tachiyomi.i18n.action_filter
import tachiyomi.i18n.action_filter_bookmarked
import tachiyomi.i18n.action_filter_unread
import tachiyomi.i18n.action_ok
import tachiyomi.i18n.action_reset
import tachiyomi.i18n.action_sort
import tachiyomi.i18n.action_sort_alpha
import tachiyomi.i18n.also_set_chapter_settings_for_library
import tachiyomi.i18n.chapter_settings
import tachiyomi.i18n.confirm_set_chapter_settings
import tachiyomi.i18n.label_downloaded
import tachiyomi.i18n.scanlator
import tachiyomi.i18n.set_chapter_settings_as_default
import tachiyomi.i18n.show_chapter_number
import tachiyomi.i18n.show_title
import tachiyomi.i18n.sort_by_number
import tachiyomi.i18n.sort_by_source
import tachiyomi.i18n.sort_by_upload_date
import tachiyomi.presentation.core.components.RadioItem
import tachiyomi.presentation.core.components.SortItem
import tachiyomi.presentation.core.components.TriStateItem
import tachiyomi.presentation.core.components.miuix.DropdownMenuItem
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.components.miuix.TextButton
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixVerticalScroll
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class ChapterSettingsPage {
    Tabs,
    Scanlators,
    Defaults,
}

@Composable
fun ChapterSettingsDialog(
    onDismissRequest: () -> Unit,
    manga: Manga? = null,
    onDownloadFilterChanged: (TriState) -> Unit,
    onUnreadFilterChanged: (TriState) -> Unit,
    onBookmarkedFilterChanged: (TriState) -> Unit,
    scanlatorFilterActive: Boolean,
    availableScanlators: Set<String>,
    excludedScanlators: Set<String>,
    onExcludedScanlatorsChanged: (Set<String>) -> Unit,
    onSortModeChanged: (Long) -> Unit,
    onDisplayModeChanged: (Long) -> Unit,
    onSetAsDefault: (applyToExistingManga: Boolean) -> Unit,
    onResetToDefault: () -> Unit,
) {
    var activePage by rememberSaveable { mutableStateOf(ChapterSettingsPage.Tabs) }
    val tabTitles = listOf(
        stringResource(MR.strings.action_filter),
        stringResource(MR.strings.action_sort),
        stringResource(MR.strings.action_display),
    )
    val pagerState = rememberPagerState { tabTitles.size }

    val context = LocalContext.current
    val downloadedOnly = remember { context.appGraph.basePreferences.downloadedOnly.get() }

    TabbedDialog(
        onDismissRequest = onDismissRequest,
        tabTitles = tabTitles,
        pagerState = pagerState,
        secondaryContent = when (activePage) {
            ChapterSettingsPage.Tabs -> null
            ChapterSettingsPage.Scanlators -> {
                {
                    ScanlatorFilterPage(
                        availableScanlators = availableScanlators,
                        excludedScanlators = excludedScanlators,
                        onDismissRequest = { activePage = ChapterSettingsPage.Tabs },
                        onConfirm = onExcludedScanlatorsChanged,
                    )
                }
            }
            ChapterSettingsPage.Defaults -> {
                {
                    SetAsDefaultPage(
                        onDismissRequest = { activePage = ChapterSettingsPage.Tabs },
                        onConfirmed = onSetAsDefault,
                    )
                }
            }
        },
        tabOverflowMenuContent = { closeMenu ->
            DropdownMenuItem(
                text = stringResource(MR.strings.set_chapter_settings_as_default),
                onClick = {
                    activePage = ChapterSettingsPage.Defaults
                    closeMenu()
                },
            )
            DropdownMenuItem(
                text = stringResource(MR.strings.action_reset),
                onClick = {
                    onResetToDefault()
                    closeMenu()
                },
            )
        },
    ) { page ->
        Column(
            modifier = Modifier
                .padding(vertical = TabbedDialogPaddings.Vertical)
                .miuixVerticalScroll(rememberScrollState()),
        ) {
            when (page) {
                0 -> {
                    FilterPage(
                        downloadFilter = manga?.downloadedFilter ?: TriState.DISABLED,
                        onDownloadFilterChanged = onDownloadFilterChanged
                            .takeUnless { downloadedOnly },
                        unreadFilter = manga?.unreadFilter ?: TriState.DISABLED,
                        onUnreadFilterChanged = onUnreadFilterChanged,
                        bookmarkedFilter = manga?.bookmarkedFilter ?: TriState.DISABLED,
                        onBookmarkedFilterChanged = onBookmarkedFilterChanged,
                        scanlatorFilterActive = scanlatorFilterActive,
                        onScanlatorFilterClicked = { activePage = ChapterSettingsPage.Scanlators },
                    )
                }
                1 -> {
                    SortPage(
                        sortingMode = manga?.sorting ?: 0,
                        sortDescending = manga?.sortDescending() ?: false,
                        onItemSelected = onSortModeChanged,
                    )
                }
                2 -> {
                    DisplayPage(
                        displayMode = manga?.displayMode ?: 0,
                        onItemSelected = onDisplayModeChanged,
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.FilterPage(
    downloadFilter: TriState,
    onDownloadFilterChanged: ((TriState) -> Unit)?,
    unreadFilter: TriState,
    onUnreadFilterChanged: (TriState) -> Unit,
    bookmarkedFilter: TriState,
    onBookmarkedFilterChanged: (TriState) -> Unit,
    scanlatorFilterActive: Boolean,
    onScanlatorFilterClicked: (() -> Unit),
) {
    TriStateItem(
        label = stringResource(MR.strings.label_downloaded),
        state = downloadFilter,
        onClick = onDownloadFilterChanged,
    )
    TriStateItem(
        label = stringResource(MR.strings.action_filter_unread),
        state = unreadFilter,
        onClick = onUnreadFilterChanged,
    )
    TriStateItem(
        label = stringResource(MR.strings.action_filter_bookmarked),
        state = bookmarkedFilter,
        onClick = onBookmarkedFilterChanged,
    )
    ScanlatorFilterItem(
        active = scanlatorFilterActive,
        onClick = onScanlatorFilterClicked,
    )
}

@Composable
fun ScanlatorFilterItem(
    active: Boolean,
    onClick: () -> Unit,
) {
    ArrowPreference(
        title = stringResource(MR.strings.scanlator),
        onClick = onClick,
        startAction = {
            Icon(
                imageVector = MaterialSymbols.Rounded.Group,
                contentDescription = null,
                tint = if (active) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantActions,
            )
        },
    )
}

@Composable
private fun ColumnScope.SortPage(
    sortingMode: Long,
    sortDescending: Boolean,
    onItemSelected: (Long) -> Unit,
) {
    listOf(
        MR.strings.sort_by_source to Manga.CHAPTER_SORTING_SOURCE,
        MR.strings.sort_by_number to Manga.CHAPTER_SORTING_NUMBER,
        MR.strings.sort_by_upload_date to Manga.CHAPTER_SORTING_UPLOAD_DATE,
        MR.strings.action_sort_alpha to Manga.CHAPTER_SORTING_ALPHABET,
    ).map { (titleRes, mode) ->
        SortItem(
            label = stringResource(titleRes),
            sortDescending = sortDescending.takeIf { sortingMode == mode },
            onClick = { onItemSelected(mode) },
        )
    }
}

@Composable
private fun ColumnScope.DisplayPage(
    displayMode: Long,
    onItemSelected: (Long) -> Unit,
) {
    listOf(
        MR.strings.show_title to Manga.CHAPTER_DISPLAY_NAME,
        MR.strings.show_chapter_number to Manga.CHAPTER_DISPLAY_NUMBER,
    ).map { (titleRes, mode) ->
        RadioItem(
            label = stringResource(titleRes),
            selected = displayMode == mode,
            onClick = { onItemSelected(mode) },
        )
    }
}

@Composable
private fun SetAsDefaultPage(
    onDismissRequest: () -> Unit,
    onConfirmed: (optionalChecked: Boolean) -> Unit,
) {
    var optionalChecked by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        SmallTitle(text = stringResource(MR.strings.chapter_settings))
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .miuixVerticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(MR.strings.confirm_set_chapter_settings))
            Card(Modifier.fillMaxWidth()) {
                CheckboxPreference(
                    title = stringResource(MR.strings.also_set_chapter_settings_for_library),
                    checked = optionalChecked,
                    onCheckedChange = { optionalChecked = it },
                )
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(MR.strings.action_cancel))
            }
            TextButton(
                onClick = {
                    onConfirmed(optionalChecked)
                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(MR.strings.action_ok))
            }
        }
    }
}
