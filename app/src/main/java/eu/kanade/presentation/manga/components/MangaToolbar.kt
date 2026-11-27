package eu.kanade.presentation.manga.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarIcon
import eu.kanade.presentation.components.downloadDropdownEntry
import eu.kanade.presentation.manga.DownloadAction
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_edit_categories
import tachiyomi.i18n.action_filter
import tachiyomi.i18n.action_migrate
import tachiyomi.i18n.action_notes
import tachiyomi.i18n.action_select_all
import tachiyomi.i18n.action_select_inverse
import tachiyomi.i18n.action_share
import tachiyomi.i18n.action_webview_refresh
import tachiyomi.i18n.manga_download
import tachiyomi.presentation.core.components.miuix.AppGlassIconDropdownMenu
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.active
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Download
import top.yukonga.miuix.kmp.icon.glass.Filter
import top.yukonga.miuix.kmp.icon.glass.MultiSelect
import top.yukonga.miuix.kmp.icon.glass.SelectAll
import top.yukonga.miuix.kmp.theme.LocalContentColor

@Composable
fun MangaToolbar(
    title: String,
    hasFilters: Boolean,
    navigateUp: () -> Unit,
    onClickFilter: () -> Unit,
    onClickShare: (() -> Unit)?,
    onClickDownload: ((DownloadAction) -> Unit)?,
    onClickEditCategory: (() -> Unit)?,
    onClickRefresh: () -> Unit,
    onClickMigrate: (() -> Unit)?,
    onClickEditNotes: () -> Unit,

    // For action mode
    actionModeCounter: Int,
    onCancelActionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onInvertSelection: () -> Unit,

    titleAlphaProvider: () -> Float,
    scrollBehavior: ScrollBehavior,
    isContentScrolled: Boolean,
    modifier: Modifier = Modifier,
) {
    val isActionMode = actionModeCounter > 0
    AppBar(
        title = title,
        isContentScrolled = isActionMode || isContentScrolled,
        titleAlpha = { if (isActionMode) 1f else titleAlphaProvider() },
        scrollBehavior = scrollBehavior,
        expandable = false,
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme
            .surfaceColorAtElevation(3.dp),
        navigateUp = navigateUp,
        actionModeActions = {
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_all),
                        icon = MiuixIcons.Glass.SelectAll,
                        onClick = onSelectAll,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_inverse),
                        icon = MiuixIcons.Glass.MultiSelect,
                        onClick = onInvertSelection,
                    ),
                ),
            )
        },
        actions = {
            if (onClickDownload != null && !isActionMode) {
                TooltipBox(text = stringResource(MR.strings.manga_download)) {
                    AppGlassIconDropdownMenu(
                        entry = downloadDropdownEntry(onClickDownload),
                        minWidth = 48.dp,
                        minHeight = 48.dp,
                    ) {
                        AppBarIcon(
                            imageVector = MiuixIcons.Glass.Download,
                            contentDescription = stringResource(MR.strings.manga_download),
                        )
                    }
                }
            }

            val filterTint = if (hasFilters) MaterialTheme.colorScheme.active else LocalContentColor.current
            AppBarActions(
                actions = buildList {
                    if (isActionMode) {
                        add(
                            AppBar.Action(
                                title = stringResource(MR.strings.action_select_all),
                                icon = MiuixIcons.Glass.SelectAll,
                                onClick = onSelectAll,
                            ),
                        )
                        add(
                            AppBar.Action(
                                title = stringResource(MR.strings.action_select_inverse),
                                icon = MiuixIcons.Glass.MultiSelect,
                                onClick = onInvertSelection,
                            ),
                        )
                        return@buildList
                    }
                    add(
                        AppBar.Action(
                            title = stringResource(MR.strings.action_filter),
                            icon = MiuixIcons.Glass.Filter,
                            iconTint = filterTint,
                            onClick = onClickFilter,
                        ),
                    )
                    add(
                        AppBar.OverflowAction(
                            title = stringResource(MR.strings.action_webview_refresh),
                            onClick = onClickRefresh,
                        ),
                    )
                    if (onClickEditCategory != null) {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_edit_categories),
                                onClick = onClickEditCategory,
                            ),
                        )
                    }
                    if (onClickMigrate != null) {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_migrate),
                                onClick = onClickMigrate,
                            ),
                        )
                    }
                    if (onClickShare != null) {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_share),
                                onClick = onClickShare,
                            ),
                        )
                    }
                    add(
                        AppBar.OverflowAction(
                            title = stringResource(MR.strings.action_notes),
                            onClick = onClickEditNotes,
                        ),
                    )
                },
            )
        },
        actionModeCounter = actionModeCounter,
        onCancelActionMode = onCancelActionMode,
    )
}
