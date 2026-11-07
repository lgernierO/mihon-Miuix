package eu.kanade.presentation.manga.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.downloadDropdownEntry
import eu.kanade.presentation.manga.DownloadAction
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.DonePrevious
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.BookmarkAdd
import mihon.icons.materialsymbols.rounded.BookmarkRemove
import mihon.icons.materialsymbols.rounded.DoneAll
import mihon.icons.materialsymbols.rounded.RemoveDone
import mihon.icons.materialsymbols.rounded.SwapCalls
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.FloatingToolbar
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TooltipAnchorPosition
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.menu.WindowIconDropdownMenu
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
                    icon = MaterialSymbols.Rounded.BookmarkAdd,
                    onClick = onBookmarkClicked,
                ),
            )
        }
        if (onRemoveBookmarkClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_remove_bookmark),
                    icon = MaterialSymbols.Rounded.BookmarkRemove,
                    onClick = onRemoveBookmarkClicked,
                ),
            )
        }
        if (onMarkAsReadClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_mark_as_read),
                    icon = MaterialSymbols.Rounded.DoneAll,
                    onClick = onMarkAsReadClicked,
                ),
            )
        }
        if (onMarkAsUnreadClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_mark_as_unread),
                    icon = MaterialSymbols.Rounded.RemoveDone,
                    onClick = onMarkAsUnreadClicked,
                ),
            )
        }
        if (onMarkPreviousAsReadClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_mark_previous_as_read),
                    icon = CustomMaterialSymbols.Rounded.DonePrevious,
                    onClick = onMarkPreviousAsReadClicked,
                ),
            )
        }
        if (onDownloadClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_download),
                    icon = MiuixIcons.Download,
                    onClick = onDownloadClicked,
                ),
            )
        }
        if (onDeleteClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_delete),
                    icon = MiuixIcons.Delete,
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
                icon = MiuixIcons.Folder,
                onClick = onChangeCategoryClicked,
            ),
        )
        add(
            BottomAction(
                title = stringResource(MR.strings.action_mark_as_read),
                icon = MaterialSymbols.Rounded.DoneAll,
                onClick = onMarkAsReadClicked,
            ),
        )
        add(
            BottomAction(
                title = stringResource(MR.strings.action_mark_as_unread),
                icon = MaterialSymbols.Rounded.RemoveDone,
                onClick = onMarkAsUnreadClicked,
            ),
        )
        if (onDownloadClicked != null) {
            add(
                BottomAction(
                    title = stringResource(MR.strings.action_download),
                    icon = MiuixIcons.Download,
                    children = downloadDropdownEntry(onDownloadClicked).items,
                ),
            )
        }
        add(
            BottomAction(
                title = stringResource(MR.strings.migrate),
                icon = MaterialSymbols.Rounded.SwapCalls,
                onClick = onMigrateClicked,
            ),
        )
        add(
            BottomAction(
                title = stringResource(MR.strings.action_delete),
                icon = MiuixIcons.Delete,
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
    AnimatedVisibility(
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
            FloatingToolbar(modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth()) {
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
                                    icon = MiuixIcons.More,
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
            WindowIconDropdownMenu(
                entry = DropdownEntry(items = action.children),
                enabled = enabled,
                minHeight = 48.dp,
                minWidth = 48.dp,
                content = icon,
            )
        } else {
            IconButton(
                onClick = { action.onClick?.invoke() },
                enabled = enabled,
                minHeight = 48.dp,
                minWidth = 48.dp,
                content = icon,
            )
        }
    }
}
