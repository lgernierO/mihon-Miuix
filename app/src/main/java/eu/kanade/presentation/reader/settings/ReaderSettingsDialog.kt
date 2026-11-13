package eu.kanade.presentation.reader.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.TabbedDialog
import eu.kanade.presentation.components.TabbedDialogPaddings
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsViewModel
import tachiyomi.i18n.MR
import tachiyomi.i18n.custom_filter
import tachiyomi.i18n.pref_category_general
import tachiyomi.i18n.pref_category_reading_mode
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixVerticalScroll
import top.yukonga.miuix.kmp.basic.Card

@Composable
fun ReaderSettingsDialog(
    onDismissRequest: () -> Unit,
    onShowMenus: () -> Unit,
    onHideMenus: () -> Unit,
    viewModel: ReaderSettingsViewModel,
) {
    val tabTitles = listOf(
        stringResource(MR.strings.pref_category_reading_mode),
        stringResource(MR.strings.pref_category_general),
        stringResource(MR.strings.custom_filter),
    )
    val pagerState = rememberPagerState { tabTitles.size }
    val isFilterPage = pagerState.settledPage == 2

    // Pager pages can be composed together during a transition. Synchronize the preview once
    // from the settled selection rather than running competing effects in each page.
    LaunchedEffect(isFilterPage) {
        if (isFilterPage) onHideMenus() else onShowMenus()
    }

    TabbedDialog(
        onDismissRequest = {
            onDismissRequest()
            onShowMenus()
        },
        tabTitles = tabTitles,
        pagerState = pagerState,
        enableWindowDim = !isFilterPage,
    ) { page ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .miuixVerticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = TabbedDialogPaddings.Vertical),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                when (page) {
                    0 -> ReadingModePage(viewModel)
                    1 -> GeneralPage(viewModel)
                    2 -> ColorFilterPage(viewModel)
                }
            }
        }
    }
}
