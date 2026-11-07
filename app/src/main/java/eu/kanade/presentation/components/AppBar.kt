package eu.kanade.presentation.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.menu.WindowIconDropdownMenu
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

val AppBarActionIconSize = 24.dp

@Composable
fun AppBar(
    title: String?,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    subtitle: String? = null,
    navigateUp: (() -> Unit)? = null,
    navigationIcon: ImageVector? = null,
    actions: @Composable RowScope.() -> Unit = {},
    actionModeCounter: Int = 0,
    onCancelActionMode: () -> Unit = {},
    actionModeActions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: ScrollBehavior? = null,
    titleColor: Color = MiuixTheme.colorScheme.onSurface,
) {
    val actionMode = actionModeCounter > 0
    val navigation: @Composable () -> Unit = {
        AppBarNavigation(actionMode, onCancelActionMode, navigateUp, navigationIcon)
    }
    val barActions: @Composable RowScope.() -> Unit = {
        if (actionMode) actionModeActions() else actions()
    }
    val color = backgroundColor ?: MiuixTheme.colorScheme.surface
    if (!actionMode && (scrollBehavior != null || navigateUp == null)) {
        TopAppBar(
            title = title.orEmpty(),
            modifier = modifier,
            subtitle = subtitle.orEmpty(),
            color = color,
            titleColor = titleColor,
            largeTitleColor = titleColor,
            navigationIcon = navigation,
            actions = barActions,
            scrollBehavior = scrollBehavior,
        )
    } else {
        val centerTitle = !actionMode && !subtitle.isNullOrBlank()
        var navigationWidth by remember { mutableIntStateOf(0) }
        var actionsWidth by remember { mutableIntStateOf(0) }
        val sideWidth = with(LocalDensity.current) { maxOf(navigationWidth, actionsWidth).toDp() }
        SmallTopAppBar(
            title = if (actionMode) actionModeCounter.toString() else title.orEmpty(),
            modifier = modifier,
            subtitle = "",
            color = color,
            titleColor = titleColor,
            titlePadding = if (centerTitle) 0.dp else TopAppBarDefaults.TitlePadding,
            navigationIcon = {
                if (centerTitle) {
                    Box(
                        modifier = Modifier.widthIn(min = sideWidth),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Box(Modifier.onSizeChanged { navigationWidth = it.width }) {
                            navigation()
                        }
                    }
                } else {
                    navigation()
                }
            },
            actions = {
                if (centerTitle) {
                    Box(
                        modifier = Modifier.widthIn(min = sideWidth),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Row(
                            modifier = Modifier.onSizeChanged { actionsWidth = it.width },
                            verticalAlignment = Alignment.CenterVertically,
                            content = barActions,
                        )
                    }
                } else {
                    barActions()
                }
            },
            scrollBehavior = scrollBehavior,
            bottomContent = {
                if (!actionMode && !subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = TopAppBarDefaults.TitlePadding,
                                end = TopAppBarDefaults.TitlePadding,
                                bottom = TopAppBarDefaults.SubtitleBottomPadding,
                            ),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        style = MiuixTheme.textStyles.body2,
                        textAlign = TextAlign.Center,
                    )
                }
            },
        )
    }
}

@Composable
private fun AppBarNavigation(
    actionMode: Boolean,
    onCancelActionMode: () -> Unit,
    navigateUp: (() -> Unit)?,
    navigationIcon: ImageVector?,
) {
    if (actionMode) {
        IconButton(
            onClick = onCancelActionMode,
        ) {
            AppBarIcon(
                imageVector = MiuixIcons.Close,
                contentDescription = stringResource(MR.strings.action_cancel),
            )
        }
    } else if (navigateUp != null) {
        IconButton(onClick = navigateUp) {
            UpIcon(navigationIcon = navigationIcon)
        }
    }
}

@Composable
fun AppBarTitle(
    title: String?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(modifier) {
        title?.let {
            Text(
                text = it,
                style = MiuixTheme.textStyles.title3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        subtitle?.let {
            Text(
                text = it,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(repeatDelayMillis = 2_000),
            )
        }
    }
}

@Composable
fun AppBarActions(actions: List<AppBar.AppBarAction>) {
    actions.filterIsInstance<AppBar.Action>().forEach { action ->
        TooltipBox(text = action.title) {
            IconButton(
                onClick = action.onClick,
                enabled = action.enabled,
            ) {
                AppBarIcon(
                    imageVector = action.icon,
                    tint = when {
                        !action.enabled -> MiuixTheme.colorScheme.disabledOnSurface
                        else -> action.iconTint ?: LocalContentColor.current
                    },
                    contentDescription = action.title,
                )
            }
        }
    }
    val overflow = actions.filterIsInstance<AppBar.OverflowAction>()
    if (overflow.isNotEmpty()) {
        TooltipBox(text = stringResource(MR.strings.action_menu_overflow_description)) {
            WindowIconDropdownMenu(
                entry = DropdownEntry(
                    items = overflow.map { action ->
                        DropdownItem(text = action.title, onClick = action.onClick)
                    },
                ),
                minWidth = 48.dp,
                minHeight = 48.dp,
            ) {
                AppBarIcon(
                    imageVector = MiuixIcons.More,
                    contentDescription = stringResource(MR.strings.action_menu_overflow_description),
                )
            }
        }
    }
}

@Composable
fun SearchToolbar(
    searchQuery: String?,
    onChangeSearchQuery: (String?) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    navigateUp: (() -> Unit)? = null,
    searchEnabled: Boolean = true,
    placeholderText: String? = null,
    onSearch: (String) -> Unit = {},
    onClickCloseSearch: () -> Unit = { onChangeSearchQuery(null) },
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: ScrollBehavior? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchAction: @Composable RowScope.() -> Unit = {
        if (searchEnabled && searchQuery == null) {
            TooltipBox(text = stringResource(MR.strings.action_search)) {
                IconButton(
                    onClick = { onChangeSearchQuery("") },
                ) {
                    AppBarIcon(
                        imageVector = MiuixIcons.Search,
                        contentDescription = stringResource(MR.strings.action_search),
                    )
                }
            }
        }
        actions()
    }
    if (searchQuery == null) {
        AppBar(
            title = title,
            modifier = modifier,
            navigateUp = navigateUp,
            actions = searchAction,
            scrollBehavior = scrollBehavior,
        )
    } else {
        SmallTopAppBar(
            title = "",
            modifier = modifier,
            titlePadding = 0.dp,
            navigationIconPadding = 0.dp,
            actions = {
                // The framework measures navigation and actions independently.
                // Keep the close button and expanding input in the same Row.
                Box(Modifier.padding(start = TopAppBarDefaults.NavigationIconPadding)) {
                    AppBarNavigation(false, {}, onClickCloseSearch, null)
                }
                ToolbarSearchField(
                    query = searchQuery,
                    onQueryChange = onChangeSearchQuery,
                    onSearch = { query ->
                        if (query.isNotBlank()) {
                            onSearch(query)
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    },
                    label = placeholderText ?: stringResource(MR.strings.action_search_hint),
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    interactionSource = interactionSource,
                )
                actions()
            },
            scrollBehavior = scrollBehavior,
        )
    }
}

@Composable
fun UpIcon(
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
) {
    AppBarIcon(
        imageVector = navigationIcon ?: MiuixIcons.Back,
        contentDescription = stringResource(MR.strings.action_bar_up_description),
        modifier = modifier,
        autoMirror = navigationIcon == null,
    )
}

sealed interface AppBar {
    sealed interface AppBarAction

    data class Action(
        val title: String,
        val icon: ImageVector,
        val iconTint: Color? = null,
        val onClick: () -> Unit,
        val enabled: Boolean = true,
    ) : AppBarAction

    data class OverflowAction(
        val title: String,
        val onClick: () -> Unit,
    ) : AppBarAction
}
