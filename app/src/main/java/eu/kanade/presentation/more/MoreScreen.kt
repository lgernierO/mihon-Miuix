package eu.kanade.presentation.more

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.i18n.categories
import tachiyomi.i18n.download_queue_summary
import tachiyomi.i18n.downloaded_only_summary
import tachiyomi.i18n.label_data_storage
import tachiyomi.i18n.label_download_queue
import tachiyomi.i18n.label_downloaded_only
import tachiyomi.i18n.label_help
import tachiyomi.i18n.label_more
import tachiyomi.i18n.label_settings
import tachiyomi.i18n.label_stats
import tachiyomi.i18n.paused
import tachiyomi.i18n.pref_category_about
import tachiyomi.i18n.pref_incognito_mode
import tachiyomi.i18n.pref_incognito_mode_summary
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.groupedCardItems
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.miuix.AppGlassTopAppBar
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.DigitalStatistics
import top.yukonga.miuix.kmp.icon.glass.Download
import top.yukonga.miuix.kmp.icon.glass.Glasses
import top.yukonga.miuix.kmp.icon.glass.Help
import top.yukonga.miuix.kmp.icon.glass.Info
import top.yukonga.miuix.kmp.icon.glass.NoNetwork
import top.yukonga.miuix.kmp.icon.glass.Save
import top.yukonga.miuix.kmp.icon.glass.SettingsOutline
import top.yukonga.miuix.kmp.icon.glass.Tags
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = MiuixScrollBehavior()
    val scrollState = rememberLazyListState()

    Scaffold(
        topBarScrollBehavior = scrollBehavior,
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            AppGlassTopAppBar(
                title = stringResource(MR.strings.label_more),
                scrollBehavior = scrollBehavior,
                scrollState = scrollState,
            )
        },
    ) { contentPadding ->
        ScrollbarLazyColumn(
            contentPadding = contentPadding,
            state = scrollState,
        ) {
            item(key = "more:spacing:0") { Spacer(Modifier.height(MoreGroupMargin)) }
            groupedCardItems(
                items = listOf("downloadedOnly", "incognito"),
                key = { "more:$it" },
            ) { row ->
                when (row) {
                    "downloadedOnly" -> {
                        SwitchPreferenceWidget(
                            title = stringResource(MR.strings.label_downloaded_only),
                            subtitle = stringResource(MR.strings.downloaded_only_summary),
                            icon = MiuixIcons.Glass.NoNetwork,
                            checked = downloadedOnly,
                            onCheckedChanged = onDownloadedOnlyChange,
                        )
                    }
                    "incognito" -> {
                        SwitchPreferenceWidget(
                            title = stringResource(MR.strings.pref_incognito_mode),
                            subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                            icon = MiuixIcons.Glass.Glasses,
                            checked = incognitoMode,
                            onCheckedChanged = onIncognitoModeChange,
                        )
                    }
                }
            }
            item(key = "more:spacing:1") { Spacer(Modifier.height(MoreGroupMargin)) }
            groupedCardItems(
                items = listOf("downloads", "categories", "statistics", "storage"),
                key = { "more:$it" },
            ) { row ->
                when (row) {
                    "downloads" -> {
                        val downloadQueueState = downloadQueueStateProvider()
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_download_queue),
                            subtitle = when (downloadQueueState) {
                                DownloadQueueState.Stopped -> null
                                is DownloadQueueState.Paused -> {
                                    val pending = downloadQueueState.pending
                                    if (pending == 0) {
                                        stringResource(MR.strings.paused)
                                    } else {
                                        "${stringResource(MR.strings.paused)} • ${
                                            pluralStringResource(
                                                MR.plurals.download_queue_summary,
                                                count = pending,
                                                pending,
                                            )
                                        }"
                                    }
                                }
                                is DownloadQueueState.Downloading -> {
                                    val pending = downloadQueueState.pending
                                    pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                                }
                            },
                            icon = MiuixIcons.Glass.Download,
                            onPreferenceClick = onClickDownloadQueue,
                        )
                    }
                    "categories" -> {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.categories),
                            icon = MiuixIcons.Glass.Tags,
                            onPreferenceClick = onClickCategories,
                        )
                    }
                    "statistics" -> {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_stats),
                            icon = MiuixIcons.Glass.DigitalStatistics,
                            onPreferenceClick = onClickStats,
                        )
                    }
                    "storage" -> {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_data_storage),
                            icon = MiuixIcons.Glass.Save,
                            onPreferenceClick = onClickDataAndStorage,
                        )
                    }
                }
            }
            item(key = "more:spacing:2") { Spacer(Modifier.height(MoreGroupMargin)) }
            groupedCardItems(
                items = listOf("settings", "about", "help"),
                key = { "more:$it" },
            ) { row ->
                when (row) {
                    "settings" -> {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_settings),
                            icon = MiuixIcons.Glass.SettingsOutline,
                            onPreferenceClick = onClickSettings,
                        )
                    }
                    "about" -> {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.pref_category_about),
                            icon = MiuixIcons.Glass.Info,
                            onPreferenceClick = onClickAbout,
                        )
                    }
                    "help" -> {
                        TextPreferenceWidget(
                            title = stringResource(MR.strings.label_help),
                            icon = MiuixIcons.Glass.Help,
                            onPreferenceClick = { uriHandler.openUri(Constants.URL_HELP) },
                        )
                    }
                }
            }
            item(key = "more:spacing:end") { Spacer(Modifier.height(MoreGroupMargin)) }
        }
    }
}

private val MoreGroupMargin = 12.dp
