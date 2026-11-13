package eu.kanade.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tachiyomi.i18n.MR
import tachiyomi.i18n.label_more
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.components.miuix.TabRow
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.utils.PagerGestureNestedScrollConnection
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.springAnimateToPage

object TabbedDialogPaddings {
    val Horizontal = 24.dp
    val Vertical = 8.dp
}

@Composable
fun TabbedDialog(
    onDismissRequest: () -> Unit,
    tabTitles: List<String>,
    modifier: Modifier = Modifier,
    tabOverflowMenuContent: (@Composable ColumnScope.(() -> Unit) -> Unit)? = null,
    pagerState: PagerState = rememberPagerState { tabTitles.size },
    enableWindowDim: Boolean = true,
    secondaryContent: (@Composable () -> Unit)? = null,
    content: @Composable (Int) -> Unit,
) {
    if (tabTitles.isEmpty()) return
    AdaptiveSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        enableWindowDim = enableWindowDim,
    ) {
        val scope = rememberCoroutineScope()

        Column(Modifier.fillMaxWidth()) {
            if (secondaryContent != null) {
                Box(Modifier.weight(1f, fill = false).fillMaxWidth()) {
                    secondaryContent()
                }
                return@Column
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TabRow(
                    tabs = tabTitles,
                    selectedTabIndex = pagerState.currentPage.coerceIn(tabTitles.indices),
                    onTabSelected = { index -> scope.launch { pagerState.springAnimateToPage(index) } },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    maxWidth = 180.dp,
                    height = 48.dp,
                )

                tabOverflowMenuContent?.let { MoreMenu(it) }
            }

            HorizontalPager(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                state = pagerState,
                // Measuring all few settings tabs keeps the natural viewport stable while
                // avoiding blank height when every tab fits in less than half a window.
                beyondViewportPageCount = tabTitles.lastIndex,
                verticalAlignment = Alignment.Top,
                // Native pager recognition yields to sliders, text selection and nested
                // horizontal controls before taking an unclaimed page swipe.
                userScrollEnabled = true,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapAnimationSpec = PagerNavigationSpringSpec,
                ),
                pageNestedScrollConnection = PagerGestureNestedScrollConnection,
                overscrollEffect = null,
                key = { it },
                pageContent = { page ->
                    key(pagerState) {
                        content(page)
                    }
                },
            )
        }
    }
}

@Composable
private fun MoreMenu(
    content: @Composable ColumnScope.(() -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        IconButton(
            onClick = { expanded = true },
        ) {
            AppBarIcon(
                imageVector = MiuixIcons.More,
                contentDescription = stringResource(MR.strings.label_more),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            content { expanded = false }
        }
    }
}
