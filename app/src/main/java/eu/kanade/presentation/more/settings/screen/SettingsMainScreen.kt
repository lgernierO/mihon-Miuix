package eu.kanade.presentation.more.settings.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.UpIcon
import eu.kanade.presentation.more.settings.screen.about.AboutScreen
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.util.LocalBackPress
import eu.kanade.presentation.util.Screen
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ChromeReaderMode
import mihon.icons.materialsymbols.rounded.Code
import mihon.icons.materialsymbols.rounded.CollectionsBookmark
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Explore
import mihon.icons.materialsymbols.rounded.Info
import mihon.icons.materialsymbols.rounded.Palette
import mihon.icons.materialsymbols.rounded.Security
import mihon.icons.materialsymbols.rounded.Storage
import mihon.icons.materialsymbols.rounded.Sync
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_search
import tachiyomi.i18n.app_name
import tachiyomi.i18n.browse
import tachiyomi.i18n.label_data_storage
import tachiyomi.i18n.label_settings
import tachiyomi.i18n.pref_advanced_summary
import tachiyomi.i18n.pref_appearance_summary
import tachiyomi.i18n.pref_backup_summary
import tachiyomi.i18n.pref_browse_summary
import tachiyomi.i18n.pref_category_about
import tachiyomi.i18n.pref_category_advanced
import tachiyomi.i18n.pref_category_appearance
import tachiyomi.i18n.pref_category_downloads
import tachiyomi.i18n.pref_category_library
import tachiyomi.i18n.pref_category_reader
import tachiyomi.i18n.pref_category_security
import tachiyomi.i18n.pref_category_tracking
import tachiyomi.i18n.pref_downloads_summary
import tachiyomi.i18n.pref_library_summary
import tachiyomi.i18n.pref_reader_summary
import tachiyomi.i18n.pref_security_summary
import tachiyomi.i18n.pref_tracking_summary
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixScroll
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.squircle.squircleBackground
import top.yukonga.miuix.kmp.theme.MiuixTheme
import cafe.adriel.voyager.core.screen.Screen as VoyagerScreen

object SettingsMainScreen : Screen() {

    @Composable
    override fun Content() {
        Content(twoPane = false)
    }

    @Composable
    fun Content(twoPane: Boolean) {
        val navigator = LocalNavigator.currentOrThrow
        val backPress = LocalBackPress.currentOrThrow
        val containerColor = MiuixTheme.colorScheme.surface
        val scrollBehavior = MiuixScrollBehavior()
        val state = rememberLazyListState()
        val groups = listOf(items.take(3), items.subList(3, 7), items.drop(7))
        val selectedScreenClass = navigator.items.firstOrNull()?.let { it::class }
        val indexSelected = if (twoPane) {
            groups.indexOfFirst { group -> group.any { it.screen::class == selectedScreenClass } }
        } else {
            -1
        }
        LaunchedEffect(indexSelected) {
            if (indexSelected >= 0) {
                state.animateScrollToItem(indexSelected)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = stringResource(MR.strings.label_settings),
                    color = containerColor,
                    navigationIcon = {
                        IconButton(onClick = backPress::invoke, minWidth = 48.dp, minHeight = 48.dp) {
                            UpIcon()
                        }
                    },
                    actions = {
                        AppBarActions(
                            listOf(
                                AppBar.Action(
                                    title = stringResource(MR.strings.action_search),
                                    icon = MiuixIcons.Search,
                                    onClick = { navigator.navigate(SettingsSearchScreen(), twoPane) },
                                ),
                            ),
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            containerColor = containerColor,
            content = { contentPadding ->
                LazyColumn(
                    state = state,
                    contentPadding = contentPadding,
                    modifier = Modifier
                        .miuixScroll()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    overscrollEffect = null,
                ) {
                    itemsIndexed(
                        items = groups,
                        key = { index, _ -> index },
                    ) { _, group ->
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .padding(top = 12.dp),
                        ) {
                            group.forEach { item ->
                                val selected = twoPane && item.screen::class == selectedScreenClass
                                TextPreferenceWidget(
                                    modifier = Modifier
                                        .then(
                                            if (selected) {
                                                Modifier.squircleBackground(
                                                    color = MiuixTheme.colorScheme.primaryContainer,
                                                    cornerRadius = 16.dp,
                                                )
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .semantics { if (twoPane) this.selected = selected },
                                    title = stringResource(item.titleRes),
                                    subtitle = item.formatSubtitle(),
                                    icon = item.icon,
                                    iconTint = if (selected) {
                                        MiuixTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MiuixTheme.colorScheme.onSurface
                                    },
                                    titleColor = if (selected) {
                                        BasicComponentDefaults.titleColor(
                                            color = MiuixTheme.colorScheme.onPrimaryContainer,
                                        )
                                    } else {
                                        BasicComponentDefaults.titleColor()
                                    },
                                    summaryColor = if (selected) {
                                        BasicComponentDefaults.summaryColor(
                                            color = MiuixTheme.colorScheme.onPrimaryContainer,
                                        )
                                    } else {
                                        BasicComponentDefaults.summaryColor()
                                    },
                                    onPreferenceClick = { navigator.navigate(item.screen, twoPane) },
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            },
        )
    }

    private fun Navigator.navigate(screen: VoyagerScreen, twoPane: Boolean) {
        if (twoPane) replaceAll(screen) else push(screen)
    }

    private data class Item(
        val titleRes: StringResource,
        val subtitleRes: StringResource? = null,
        val formatSubtitle: @Composable () -> String? = { subtitleRes?.let { stringResource(it) } },
        val icon: ImageVector,
        val screen: VoyagerScreen,
    )

    private val items = listOf(
        Item(
            titleRes = MR.strings.pref_category_appearance,
            subtitleRes = MR.strings.pref_appearance_summary,
            icon = MaterialSymbols.Rounded.Palette,
            screen = SettingsAppearanceScreen,
        ),
        Item(
            titleRes = MR.strings.pref_category_library,
            subtitleRes = MR.strings.pref_library_summary,
            icon = MaterialSymbols.Rounded.CollectionsBookmark,
            screen = SettingsLibraryScreen,
        ),
        Item(
            titleRes = MR.strings.pref_category_reader,
            subtitleRes = MR.strings.pref_reader_summary,
            icon = MaterialSymbols.AutoMirroredRounded.ChromeReaderMode,
            screen = SettingsReaderScreen,
        ),
        Item(
            titleRes = MR.strings.pref_category_downloads,
            subtitleRes = MR.strings.pref_downloads_summary,
            icon = MaterialSymbols.Rounded.Download,
            screen = SettingsDownloadScreen,
        ),
        Item(
            titleRes = MR.strings.pref_category_tracking,
            subtitleRes = MR.strings.pref_tracking_summary,
            icon = MaterialSymbols.Rounded.Sync,
            screen = SettingsTrackingScreen,
        ),
        Item(
            titleRes = MR.strings.browse,
            subtitleRes = MR.strings.pref_browse_summary,
            icon = MaterialSymbols.Rounded.Explore,
            screen = SettingsBrowseScreen,
        ),
        Item(
            titleRes = MR.strings.label_data_storage,
            subtitleRes = MR.strings.pref_backup_summary,
            icon = MaterialSymbols.Rounded.Storage,
            screen = SettingsDataScreen,
        ),
        Item(
            titleRes = MR.strings.pref_category_security,
            subtitleRes = MR.strings.pref_security_summary,
            icon = MaterialSymbols.Rounded.Security,
            screen = SettingsSecurityScreen,
        ),
        Item(
            titleRes = MR.strings.pref_category_advanced,
            subtitleRes = MR.strings.pref_advanced_summary,
            icon = MaterialSymbols.Rounded.Code,
            screen = SettingsAdvancedScreen,
        ),
        Item(
            titleRes = MR.strings.pref_category_about,
            formatSubtitle = {
                "${stringResource(MR.strings.app_name)} ${AboutScreen.getVersionName(withBuildDate = false)}"
            },
            icon = MaterialSymbols.Rounded.Info,
            screen = AboutScreen,
        ),
    )
}
