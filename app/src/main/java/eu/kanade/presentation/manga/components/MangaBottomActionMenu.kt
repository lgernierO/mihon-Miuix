package eu.kanade.presentation.manga.components

import androidx.compose.animation.CapturedAnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.downloadDropdownEntry
import eu.kanade.presentation.manga.DownloadAction
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_bookmark
import tachiyomi.i18n.action_delete
import tachiyomi.i18n.action_download
import tachiyomi.i18n.action_mark_as_read
import tachiyomi.i18n.action_mark_as_unread
import tachiyomi.i18n.action_mark_previous_as_read
import tachiyomi.i18n.action_move_category
import tachiyomi.i18n.action_remove_bookmark
import tachiyomi.i18n.label_more
import tachiyomi.i18n.migrate
import tachiyomi.presentation.core.components.miuix.AppGlassIconDropdownMenu
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.components.miuix.LocalMiuixBackdrop
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.TooltipAnchorPosition
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.glass.GlassFloatingToolbar
// import top.yukonga.miuix.kmp.material.GlassShadows // Restore with the toolbar shadow below.
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.AddBookmark
import top.yukonga.miuix.kmp.icon.glass.BookmarkOutline
import top.yukonga.miuix.kmp.icon.glass.Delete
import top.yukonga.miuix.kmp.icon.glass.Download
import top.yukonga.miuix.kmp.icon.glass.FolderOutline
import top.yukonga.miuix.kmp.icon.glass.MarkAllAsRead
import top.yukonga.miuix.kmp.icon.glass.MarkAsRead
import top.yukonga.miuix.kmp.icon.glass.MarkAsUnread
import top.yukonga.miuix.kmp.icon.glass.More
import top.yukonga.miuix.kmp.icon.glass.Replace
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MangaBottomActionMenu(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onBookmarkClicked: (() -> Unit)? = null,
    onRemoveBookmarkClicked: (() -> Unit)? = null,
    onMarkAsReadClicked: (() -> Unit)? = null,
    onMarkAsUnreadClicked: (() -> Unit)? = null,
    onMarkPreviousAsReadClicked: (() -> Unit)? = null,
    onDownloadClicked: (() -> Unit)? = null,
    onDeleteClicked: (() -> Unit)? = null,
) {
    val actions = buildList {
        if (onBookmarkClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_bookmark),
                    icon = MiuixIcons.Glass.AddBookmark,
                    onClick = onBookmarkClicked,
                ),
            )
        }
        if (onRemoveBookmarkClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_remove_bookmark),
                    icon = MiuixIcons.Glass.BookmarkOutline,
                    onClick = onRemoveBookmarkClicked,
                ),
            )
        }
        if (onMarkAsReadClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_mark_as_read),
                    icon = MiuixIcons.Glass.MarkAllAsRead,
                    onClick = onMarkAsReadClicked,
                ),
            )
        }
        if (onMarkAsUnreadClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_mark_as_unread),
                    icon = MiuixIcons.Glass.MarkAsUnread,
                    onClick = onMarkAsUnreadClicked,
                ),
            )
        }
        if (onMarkPreviousAsReadClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_mark_previous_as_read),
                    icon = MiuixIcons.Glass.MarkAsRead,
                    onClick = onMarkPreviousAsReadClicked,
                ),
            )
        }
        if (onDownloadClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_download),
                    icon = MiuixIcons.Glass.Download,
                    onClick = onDownloadClicked,
                ),
            )
        }
        if (onDeleteClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_delete),
                    icon = MiuixIcons.Glass.Delete,
                    onClick = onDeleteClicked,
                ),
            )
        }
    }
    SelectionActionToolbar(visible = visible, actions = actions, modifier = modifier)
}

@Composable
fun LibraryBottomActionMenu(
    visible: Boolean,
    onChangeCategoryClicked: () -> Unit,
    onMarkAsReadClicked: () -> Unit,
    onMarkAsUnreadClicked: () -> Unit,
    onDownloadClicked: ((DownloadAction) -> Unit)?,
    onDeleteClicked: () -> Unit,
    onMigrateClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = buildList {
        add(
            BottomAction(
                title = stringResource(MR.strings.action_move_category),
                icon = MiuixIcons.Glass.FolderOutline,
                onClick = onChangeCategoryClicked,
            ),
        )
        add(
            BottomAction(
                title = stringResource(MR.strings.action_mark_as_read),
                icon = MiuixIcons.Glass.MarkAllAsRead,
                onClick = onMarkAsReadClicked,
            ),
        )
        add(
            BottomAction(
                title = stringResource(MR.strings.action_mark_as_unread),
                icon = MiuixIcons.Glass.MarkAsUnread,
                onClick = onMarkAsUnreadClicked,
            ),
        )
        if (onDownloadClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_download),
                    icon = MiuixIcons.Glass.Download,
                    children = downloadDropdownEntry(onDownloadClicked).items,
                ),
            )
        }
        add(
            BottomAction(
                title = stringResource(MR.strings.migrate),
                icon = MiuixIcons.Glass.Replace,
                onClick = onMigrateClicked,
            ),
        )
        add(
            BottomAction(
                title = stringResource(MR.strings.action_delete),
                icon = MiuixIcons.Glass.Delete,
                onClick = onDeleteClicked,
            ),
        )
    }
    SelectionActionToolbar(visible = visible, actions = actions, modifier = modifier)
}

private data class BottomAction(
    val title: String,
    val icon: ImageVector,
    val onClick: (() -> Unit)? = null,
    val children: List<DropdownItem>? = null,
)

@Composable
private fun SelectionActionToolbar(
    visible: Boolean,
    actions: List<BottomAction>,
    modifier: Modifier = Modifier,
) {
    CapturedAnimatedVisibility(
        visible = visible && actions.isNotEmpty(),
        modifier = modifier,
        enter = expandVertically(expandFrom = Alignment.Bottom),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.navigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Reserve the native toolbar's outside padding and the row's inner padding.
            val actionLimit = ((maxWidth.coerceAtMost(400.dp) - 40.dp) / 48.dp).toInt().coerceIn(1, 5)
            val directActions = if (actions.size > actionLimit) actions.take(actionLimit - 1) else actions
            val overflowActions = actions.drop(directActions.size)
            GlassFloatingToolbar(
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                backdrop = LocalMiuixBackdrop.current,
                // shadow = GlassShadows.Regular,
                shadow = null,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    directActions.forEach { action ->
                        key(action.title) {
                            SelectionActionButton(action = action, enabled = visible)
                        }
                    }
                    if (overflowActions.isNotEmpty()) {
                        key("overflow") {
                            SelectionActionButton(
                                BottomAction(
                                    title = stringResource(MR.strings.label_more),
                                    icon = MiuixIcons.Glass.More,
                                    children = overflowActions.map { action ->
                                        DropdownItem(
                                            text = action.title,
                                            onClick = action.onClick,
                                            children = action.children,
                                            icon = { iconModifier ->
                                                Icon(
                                                    imageVector = action.icon,
                                                    contentDescription = null,
                                                    modifier = iconModifier.size(24.dp),
                                                    tint = MiuixTheme.colorScheme.onSurfaceContainer,
                                                )
                                            },
                                        )
                                    },
                                ),
                                enabled = visible,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionActionButton(action: BottomAction, enabled: Boolean) {
    TooltipBox(text = action.title, positioning = TooltipAnchorPosition.Above, enabled = enabled) {
        val icon: @Composable () -> Unit = {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                modifier = Modifier.size(24.dp),
                tint = MiuixTheme.colorScheme.onSurfaceContainer,
            )
        }
        if (action.children != null) {
            AppGlassIconDropdownMenu(
                entry = DropdownEntry(items = action.children),
                enabled = enabled,
                minHeight = 48.dp,
                minWidth = 48.dp,
                backgroundColor = Color.Transparent,
                content = icon,
            )
        } else {
            IconButton(
                onClick = { action.onClick?.invoke() },
                enabled = enabled,
                backgroundColor = Color.Transparent,
                content = icon,
            )
        }
    }
}
