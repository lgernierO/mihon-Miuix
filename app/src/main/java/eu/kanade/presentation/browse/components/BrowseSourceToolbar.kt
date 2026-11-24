package eu.kanade.presentation.browse.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarIcon
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.Source
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_display_comfortable_grid
import tachiyomi.i18n.action_display_grid
import tachiyomi.i18n.action_display_list
import tachiyomi.i18n.action_display_mode
import tachiyomi.i18n.action_open_in_web_view
import tachiyomi.i18n.action_settings
import tachiyomi.i18n.label_help
import tachiyomi.presentation.core.components.miuix.AppGlassIconDropdownMenu
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.source.local.LocalSource
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.ScrollBehavior as TopAppBarScrollBehavior
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.GridView
import top.yukonga.miuix.kmp.icon.glass.ListView
import top.yukonga.miuix.kmp.icon.glass.Thumbnail

@Composable
fun BrowseSourceToolbar(
    searchQuery: String?,
    onSearchQueryChange: (String?) -> Unit,
    source: Source?,
    displayMode: LibraryDisplayMode,
    onDisplayModeChange: (LibraryDisplayMode) -> Unit,
    navigateUp: () -> Unit,
    onWebViewClick: () -> Unit,
    onHelpClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearch: (String) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    // Avoid capturing unstable source in actions lambda
    val title = source?.name
    val isLocalSource = source is LocalSource
    val isConfigurableSource = source is ConfigurableSource

    SearchToolbar(
        navigateUp = navigateUp,
        title = title.orEmpty(),
        searchQuery = searchQuery,
        onChangeSearchQuery = onSearchQueryChange,
        onSearch = onSearch,
        onClickCloseSearch = navigateUp,
        actions = {
            TooltipBox(text = stringResource(MR.strings.action_display_mode)) {
                AppGlassIconDropdownMenu(
                    entry = DropdownEntry(
                        items = listOf(
                            DropdownItem(
                                text = stringResource(MR.strings.action_display_comfortable_grid),
                                selected = displayMode == LibraryDisplayMode.ComfortableGrid,
                                onClick = { onDisplayModeChange(LibraryDisplayMode.ComfortableGrid) },
                            ),
                            DropdownItem(
                                text = stringResource(MR.strings.action_display_grid),
                                selected = displayMode == LibraryDisplayMode.CompactGrid ||
                                    displayMode == LibraryDisplayMode.CoverOnlyGrid,
                                onClick = { onDisplayModeChange(LibraryDisplayMode.CompactGrid) },
                            ),
                            DropdownItem(
                                text = stringResource(MR.strings.action_display_list),
                                selected = displayMode == LibraryDisplayMode.List,
                                onClick = { onDisplayModeChange(LibraryDisplayMode.List) },
                            ),
                        ),
                    ),
                    minWidth = 48.dp,
                    minHeight = 48.dp,
                ) {
                    AppBarIcon(
                        imageVector = when (displayMode) {
                            LibraryDisplayMode.List -> MiuixIcons.Glass.ListView
                            LibraryDisplayMode.ComfortableGrid -> MiuixIcons.Glass.GridView
                            LibraryDisplayMode.CompactGrid,
                            LibraryDisplayMode.CoverOnlyGrid,
                            -> MiuixIcons.Glass.Thumbnail
                        },
                        contentDescription = stringResource(MR.strings.action_display_mode),
                    )
                }
            }
            AppBarActions(
                actions = buildList {
                    if (isLocalSource) {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.label_help),
                                onClick = onHelpClick,
                            ),
                        )
                    } else {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_open_in_web_view),
                                onClick = onWebViewClick,
                            ),
                        )
                    }
                    if (isConfigurableSource) {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_settings),
                                onClick = onSettingsClick,
                            ),
                        )
                    }
                },
            )
        },
        scrollBehavior = scrollBehavior,
    )
}
