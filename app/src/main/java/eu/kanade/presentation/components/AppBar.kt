package eu.kanade.presentation.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_bar_up_description
import tachiyomi.i18n.action_cancel
import tachiyomi.i18n.action_menu_overflow_description
import tachiyomi.i18n.action_search
import tachiyomi.i18n.action_search_hint
import tachiyomi.presentation.core.components.miuix.AppGlassIconDropdownMenu
import tachiyomi.presentation.core.components.miuix.AppGlassTopAppBar
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Back
import top.yukonga.miuix.kmp.icon.glass.Close
import top.yukonga.miuix.kmp.icon.glass.More
import top.yukonga.miuix.kmp.icon.glass.Search
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

val AppBarActionIconSize = 28.dp

// 48dp touch slots plus this gap keep compact circular surfaces visually separate.
val AppBarActionSpacing = 2.dp

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
    expandable: Boolean = scrollBehavior != null || navigateUp == null,
    isContentScrolled: Boolean? = null,
    titleAlpha: () -> Float = { 1f },
    backgroundAlpha: () -> Float = { 1f },
    bottomContent: @Composable () -> Unit = {},
) {
    val actionMode = actionModeCounter > 0
    val navigation: @Composable () -> Unit = {
        AppBarNavigation(actionMode, onCancelActionMode, navigateUp, navigationIcon)
    }
    val barActions: @Composable RowScope.() -> Unit = {
        // Space the complete sequence, including SearchToolbar's leading search button and menus.
        // AppBarActions intentionally emits its children directly into this row.
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppBarActionSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (actionMode) actionModeActions() else actions()
        }
    }
    val color = backgroundColor ?: MiuixTheme.colorScheme.surface
    AppGlassTopAppBar(
        title = if (actionMode) actionModeCounter.toString() else title.orEmpty(),
        modifier = modifier,
        subtitle = if (actionMode) "" else subtitle.orEmpty(),
        color = color,
        expandable = !actionMode && expandable,
        isContentScrolled = if (actionMode) true else isContentScrolled,
        scrollBehavior = scrollBehavior,
        titleAlpha = titleAlpha,
        backgroundAlpha = backgroundAlpha,
        navigationIcon = navigation,
        actions = barActions,
        bottomContent = bottomContent,
    )
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
                imageVector = MiuixIcons.Glass.Close,
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
            AppGlassIconDropdownMenu(
                entry = DropdownEntry(
                    items = overflow.map { action ->
                        DropdownItem(text = action.title, onClick = action.onClick)
                    },
                ),
                minWidth = 48.dp,
                minHeight = 48.dp,
            ) {
                AppBarIcon(
                    imageVector = MiuixIcons.Glass.More,
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
    bottomContent: @Composable () -> Unit = {},
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
                        imageVector = MiuixIcons.Glass.Search,
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
            bottomContent = bottomContent,
        )
    } else {
        AppGlassTopAppBar(
            title = "",
            modifier = modifier,
            expandable = false,
            actions = {
                // The framework measures navigation and actions independently.
                // Keep the close button and expanding input in the same Row.
                Box(Modifier.padding(start = TopAppBarDefaults.NavigationIconPadding, end = 8.dp)) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppBarActionSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            },
            scrollBehavior = scrollBehavior,
            bottomContent = bottomContent,
        )
    }
}

@Composable
fun UpIcon(
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
) {
    AppBarIcon(
        imageVector = navigationIcon ?: MiuixIcons.Glass.Back,
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
