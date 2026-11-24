package eu.kanade.presentation.reader.appbars

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_bookmark
import tachiyomi.i18n.action_open_in_browser
import tachiyomi.i18n.action_open_in_web_view
import tachiyomi.i18n.action_remove_bookmark
import tachiyomi.i18n.action_share
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.AddBookmark
import top.yukonga.miuix.kmp.icon.glass.BookmarkOutline
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ReaderTopBar(
    mangaTitle: String?,
    chapterTitle: String?,
    navigateUp: () -> Unit,
    bookmarked: Boolean,
    onToggleBookmarked: () -> Unit,
    onOpenInWebView: (() -> Unit)?,
    onOpenInBrowser: (() -> Unit)?,
    onShare: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    AppBar(
        modifier = modifier,
        backgroundColor = MiuixTheme.colorScheme.surfaceContainer,
        isContentScrolled = true,
        title = mangaTitle,
        subtitle = chapterTitle,
        navigateUp = navigateUp,
        actions = {
            AppBarActions(
                actions = buildList {
                    add(
                        AppBar.Action(
                            title = stringResource(
                                if (bookmarked) {
                                    MR.strings.action_remove_bookmark
                                } else {
                                    MR.strings.action_bookmark
                                },
                            ),
                            icon = if (bookmarked) {
                                MiuixIcons.Glass.BookmarkOutline
                            } else {
                                MiuixIcons.Glass.AddBookmark
                            },
                            iconTint = MiuixTheme.colorScheme.primary.takeIf { bookmarked },
                            onClick = onToggleBookmarked,
                        ),
                    )
                    onOpenInWebView?.let {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_open_in_web_view),
                                onClick = it,
                            ),
                        )
                    }
                    onOpenInBrowser?.let {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_open_in_browser),
                                onClick = it,
                            ),
                        )
                    }
                    onShare?.let {
                        add(
                            AppBar.OverflowAction(
                                title = stringResource(MR.strings.action_share),
                                onClick = it,
                            ),
                        )
                    }
                },
            )
        },
    )
}
