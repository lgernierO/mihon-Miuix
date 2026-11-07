package eu.kanade.tachiyomi.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import eu.kanade.presentation.util.Screen
import eu.kanade.presentation.util.isTabletUi
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.download.DownloadQueueScreen
import eu.kanade.tachiyomi.ui.history.HistoryTab
import eu.kanade.tachiyomi.ui.library.LibraryTab
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Autorenew
import mihon.icons.materialsymbols.rounded.CollectionsBookmark
import mihon.icons.materialsymbols.rounded.Explore
import mihon.icons.materialsymbols.rounded.MoreVert
import mihon.icons.materialsymbols.rounded.Schedule
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text

object HomeScreen : Screen() {

    private val librarySearchEvent = Channel<String>()
    private val openTabEvent = Channel<Tab>()
    private val showBottomNavEvent = Channel<Boolean>()

    @Suppress("ConstPropertyName")
    private const val TabFadeDuration = 200

    @Suppress("ConstPropertyName")
    private const val TabNavigatorKey = "HomeTabs"

    private val TABS = listOf(
        LibraryTab,
        UpdatesTab,
        HistoryTab,
        BrowseTab,
        MoreTab,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TabNavigator(
            tab = LibraryTab,
            key = TabNavigatorKey,
        ) { tabNavigator ->
            // Provide usable navigator to content screen
            CompositionLocalProvider(LocalNavigator provides navigator) {
                val tabletUi = isTabletUi()
                var showCompactNavigation by rememberSaveable { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    showBottomNavEvent.receiveAsFlow().collectLatest { show ->
                        showCompactNavigation = show
                    }
                }

                Row(Modifier.fillMaxSize()) {
                    if (tabletUi) {
                        NavigationRail {
                            TABS.fastForEach { tab ->
                                HomeNavigationRailItem(tab)
                            }
                        }
                    }
                    Scaffold(
                        modifier = Modifier.weight(1f),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            AnimatedVisibility(
                                visible = !tabletUi && showCompactNavigation,
                                enter = expandVertically(),
                                exit = shrinkVertically(),
                            ) {
                                NavigationBar {
                                    TABS.fastForEach { tab ->
                                        HomeNavigationBarItem(tab)
                                    }
                                }
                            }
                        },
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(it)
                                .consumeWindowInsets(it),
                        ) {
                            AnimatedContent(
                                targetState = tabNavigator.current,
                                contentKey = { tab -> tab.key },
                                transitionSpec = {
                                    materialFadeThroughIn(
                                        initialScale = 1f,
                                        durationMillis = TabFadeDuration,
                                    ) togetherWith materialFadeThroughOut(durationMillis = TabFadeDuration)
                                },
                                label = "tabContent",
                            ) { tab ->
                                tabNavigator.saveableState(key = "currentTab", tab) {
                                    tab.Content()
                                }
                            }
                        }
                    }
                }
            }

            val goToLibraryTab = { tabNavigator.current = LibraryTab }

            BackHandler(enabled = tabNavigator.current != LibraryTab, onBack = goToLibraryTab)

            LaunchedEffect(Unit) {
                launch {
                    librarySearchEvent.receiveAsFlow().collectLatest {
                        goToLibraryTab()
                        LibraryTab.search(it)
                    }
                }
                launch {
                    openTabEvent.receiveAsFlow().collectLatest {
                        tabNavigator.current = when (it) {
                            is Tab.Library -> LibraryTab
                            Tab.Updates -> UpdatesTab
                            Tab.History -> HistoryTab
                            is Tab.Browse -> {
                                if (it.toExtensions) {
                                    BrowseTab.showExtension()
                                }
                                BrowseTab
                            }
                            is Tab.More -> MoreTab
                        }

                        if (it is Tab.Library && it.mangaIdToOpen != null) {
                            navigator.push(MangaScreen(it.mangaIdToOpen))
                        }
                        if (it is Tab.More && it.toDownloads) {
                            navigator.push(DownloadQueueScreen)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun HomeNavigationRailItem(
        tab: eu.kanade.presentation.util.Tab,
    ) {
        val tabNavigator = LocalTabNavigator.current
        val selected = tabNavigator.current::class == tab::class
        val onSelect = rememberTabSelection(tab)
        NavigationRailItem(
            selected = selected,
            onClick = onSelect,
            icon = tab.navigationIcon,
            label = tab.options.title,
            badge = tabBadge(tab),
        )
    }

    @Composable
    private fun RowScope.HomeNavigationBarItem(tab: eu.kanade.presentation.util.Tab) {
        val tabNavigator = LocalTabNavigator.current
        NavigationBarItem(
            selected = tabNavigator.current::class == tab::class,
            onClick = rememberTabSelection(tab),
            icon = tab.navigationIcon,
            label = tab.options.title,
            modifier = Modifier.weight(1f),
            badge = tabBadge(tab),
        )
    }

    @Composable
    private fun rememberTabSelection(tab: eu.kanade.presentation.util.Tab): () -> Unit {
        val tabNavigator = LocalTabNavigator.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        return {
            if (tabNavigator.current::class != tab::class) {
                tabNavigator.current = tab
            } else {
                scope.launch { tab.onReselect(navigator) }
            }
        }
    }

    private val eu.kanade.presentation.util.Tab.navigationIcon: ImageVector
        get() = when (this) {
            LibraryTab -> MaterialSymbols.Rounded.CollectionsBookmark
            UpdatesTab -> MaterialSymbols.Rounded.Autorenew
            HistoryTab -> MaterialSymbols.Rounded.Schedule
            BrowseTab -> MaterialSymbols.Rounded.Explore
            else -> MaterialSymbols.Rounded.MoreVert
        }

    @Composable
    private fun tabBadge(tab: eu.kanade.presentation.util.Tab): (@Composable () -> Unit)? {
        val context = LocalContext.current
        val count by produceState(initialValue = 0, tab) {
            val graph = context.appGraph
            when (tab) {
                is UpdatesTab -> {
                    combine(
                        graph.libraryPreferences.newShowUpdatesCount.changes(),
                        graph.libraryPreferences.newUpdatesCount.changes(),
                    ) { show, count ->
                        if (show) count else 0
                    }
                        .collectLatest { value = it }
                }

                is BrowseTab -> {
                    graph.sourcePreferences.extensionUpdatesCount.changes()
                        .collectLatest { value = it }
                }

                else -> value = 0
            }
        }
        if (count <= 0) return null
        return {
            Badge {
                val desc = when (tab) {
                    is UpdatesTab -> pluralStringResource(
                        MR.plurals.notification_chapters_generic,
                        count = count,
                        count,
                    )

                    is BrowseTab -> pluralStringResource(
                        MR.plurals.update_check_notification_ext_updates,
                        count = count,
                        count,
                    )

                    else -> null
                }
                Text(
                    text = count.toString(),
                    modifier = Modifier.semantics {
                        if (desc != null) contentDescription = desc
                    },
                )
            }
        }
    }

    suspend fun search(query: String) {
        librarySearchEvent.send(query)
    }

    suspend fun openTab(tab: Tab) {
        openTabEvent.send(tab)
    }

    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }

    sealed interface Tab {
        data class Library(val mangaIdToOpen: Long? = null) : Tab
        data object Updates : Tab
        data object History : Tab
        data class Browse(val toExtensions: Boolean = false) : Tab
        data class More(val toDownloads: Boolean) : Tab
    }
}
