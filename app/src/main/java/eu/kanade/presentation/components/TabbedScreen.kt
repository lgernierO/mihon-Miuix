package eu.kanade.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.launch
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.miuix.SnackbarHost
import tachiyomi.presentation.core.components.miuix.TabRow
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.utils.PagerGestureNestedScrollConnection
import top.yukonga.miuix.kmp.utils.PagerInterceptionMode
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.pagerGestureOverride
import top.yukonga.miuix.kmp.utils.springAnimateToPage

@Composable
fun TabbedScreen(
    titleRes: StringResource,
    tabs: List<TabContent>,
    state: PagerState = rememberPagerState { tabs.size },
    searchQuery: String? = null,
    onChangeSearchQuery: (String?) -> Unit = {},
) {
    if (tabs.isEmpty()) return
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val flingBehavior = PagerDefaults.flingBehavior(state, snapAnimationSpec = PagerNavigationSpringSpec)

    Scaffold(
        topBar = {
            val tab = tabs[state.currentPage]
            val searchEnabled = tab.searchEnabled

            SearchToolbar(
                title = stringResource(titleRes),
                searchEnabled = searchEnabled,
                searchQuery = if (searchEnabled) searchQuery else null,
                onChangeSearchQuery = onChangeSearchQuery,
                actions = { AppBarActions(tab.actions) },
                scrollBehavior = it,
                bottomContent = {
                    Column {
                        TabRow(
                            tabs = tabs.map { tab ->
                                val title = stringResource(tab.titleRes)
                                tab.badgeNumber?.let { "$title ($it)" } ?: title
                            },
                            selectedTabIndex = state.currentPage,
                            onTabSelected = { index -> scope.launch { state.springAnimateToPage(index) } },
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            maxWidth = 180.dp,
                            height = 48.dp,
                        )
                        tab.headerContent()
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        HorizontalPager(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                )
                .pagerGestureOverride(
                    pagerState = state,
                    flingBehavior = flingBehavior,
                    mode = PagerInterceptionMode.CrossAxis,
                ),
            state = state,
            flingBehavior = flingBehavior,
            verticalAlignment = Alignment.Top,
            userScrollEnabled = false,
            pageNestedScrollConnection = PagerGestureNestedScrollConnection,
            overscrollEffect = null,
        ) { page ->
            tabs[page].content(
                PaddingValues(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding(),
                ),
                snackbarHostState,
            )
        }
    }
}

data class TabContent(
    val titleRes: StringResource,
    val badgeNumber: Int? = null,
    val searchEnabled: Boolean = false,
    val actions: List<AppBar.AppBarAction> = listOf(),
    val headerContent: @Composable () -> Unit = {},
    val content: @Composable (contentPadding: PaddingValues, snackbarHostState: SnackbarHostState) -> Unit,
)
