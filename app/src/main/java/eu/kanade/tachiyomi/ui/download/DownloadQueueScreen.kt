package eu.kanade.tachiyomi.ui.download

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarIcon
import eu.kanade.presentation.download.DownloadQueueList
import eu.kanade.presentation.util.Screen
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.roundedfilled.Pause
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.miuix.SmallExtendedFloatingActionButton
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.menu.WindowIconCascadingDropdownMenu

object DownloadQueueScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = metroViewModel<DownloadQueueViewModel>()
        val downloadList by viewModel.state.collectAsStateWithLifecycle()
        val downloadCount by remember {
            derivedStateOf { downloadList.sumOf { it.downloads.size } }
        }

        val scrollBehavior = MiuixScrollBehavior()
        var fabExpanded by remember { mutableStateOf(true) }
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    fabExpanded = available.y >= 0
                    return Offset.Zero
                }
            }
        }

        Scaffold(
            topBarScrollBehavior = scrollBehavior,
            topBar = {
                AppBar(
                    title = stringResource(MR.strings.label_download_queue).let { title ->
                        if (downloadCount > 0) "$title ($downloadCount)" else title
                    },
                    navigateUp = navigator::pop,
                    actions = {
                        if (downloadList.isNotEmpty()) {
                            TooltipBox(text = stringResource(MR.strings.action_sort)) {
                                WindowIconCascadingDropdownMenu(
                                    entry = DropdownEntry(
                                        items = listOf(
                                            DropdownItem(
                                                text = stringResource(MR.strings.action_order_by_upload_date),
                                                children = listOf(
                                                    DropdownItem(
                                                        text = stringResource(MR.strings.action_newest),
                                                        onClick = {
                                                            viewModel.reorderQueue({ it.chapter.dateUpload }, true)
                                                        },
                                                    ),
                                                    DropdownItem(
                                                        text = stringResource(MR.strings.action_oldest),
                                                        onClick = {
                                                            viewModel.reorderQueue({ it.chapter.dateUpload }, false)
                                                        },
                                                    ),
                                                ),
                                            ),
                                            DropdownItem(
                                                text = stringResource(MR.strings.action_order_by_chapter_number),
                                                children = listOf(
                                                    DropdownItem(
                                                        text = stringResource(MR.strings.action_asc),
                                                        onClick = {
                                                            viewModel.reorderQueue({ it.chapter.chapterNumber }, false)
                                                        },
                                                    ),
                                                    DropdownItem(
                                                        text = stringResource(MR.strings.action_desc),
                                                        onClick = {
                                                            viewModel.reorderQueue({ it.chapter.chapterNumber }, true)
                                                        },
                                                    ),
                                                ),
                                            ),
                                        ),
                                    ),
                                    minWidth = 48.dp,
                                    minHeight = 48.dp,
                                ) {
                                    AppBarIcon(
                                        imageVector = MiuixIcons.Sort,
                                        contentDescription = stringResource(MR.strings.action_sort),
                                    )
                                }
                            }

                            AppBarActions(
                                listOf(
                                    AppBar.OverflowAction(
                                        title = stringResource(MR.strings.action_cancel_all),
                                        onClick = { viewModel.clearQueue() },
                                    ),
                                ),
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                val isRunning by viewModel.isDownloaderRunning.collectAsStateWithLifecycle()
                SmallExtendedFloatingActionButton(
                    text = {
                        val id = if (isRunning) {
                            MR.strings.action_pause
                        } else {
                            MR.strings.action_resume
                        }
                        Text(text = stringResource(id))
                    },
                    icon = {
                        val icon = if (isRunning) {
                            MaterialSymbols.RoundedFilled.Pause
                        } else {
                            MaterialSymbols.RoundedFilled.PlayArrow
                        }
                        Icon(imageVector = icon, contentDescription = null)
                    },
                    onClick = {
                        if (isRunning) {
                            viewModel.pauseDownloads()
                        } else {
                            viewModel.startDownloads()
                        }
                    },
                    expanded = fabExpanded,
                    modifier = Modifier.animateFloatingActionButton(
                        visible = downloadList.isNotEmpty(),
                        alignment = Alignment.BottomEnd,
                    ),
                )
            },
        ) { contentPadding ->
            if (downloadList.isEmpty()) {
                EmptyScreen(
                    stringRes = MR.strings.information_no_downloads,
                    modifier = Modifier.padding(contentPadding),
                )
                return@Scaffold
            }

            DownloadQueueList(
                sections = downloadList,
                lazyListState = rememberLazyListState(),
                contentPadding = contentPadding,
                onReorder = viewModel::reorder,
                onMove = viewModel::move,
                onMoveSeries = viewModel::moveSeries,
                onCancel = { viewModel.cancel(listOf(it)) },
                onCancelSeries = viewModel::cancelSeries,
                modifier = Modifier.nestedScroll(nestedScrollConnection),
            )
        }
    }
}
