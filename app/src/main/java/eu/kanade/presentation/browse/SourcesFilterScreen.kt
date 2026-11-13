package eu.kanade.presentation.browse

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.browse.components.SourceIcon
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.tachiyomi.ui.browse.source.SourcesFilterViewModel
import eu.kanade.tachiyomi.util.system.LocaleHelper
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.i18n.label_sources
import tachiyomi.i18n.source_filter_empty_screen
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import top.yukonga.miuix.kmp.preference.CheckboxLocation
import top.yukonga.miuix.kmp.preference.CheckboxPreference

@Composable
fun SourcesFilterScreen(
    navigateUp: () -> Unit,
    state: SourcesFilterViewModel.State.Success,
    onClickLanguage: (String) -> Unit,
    onClickSource: (Source) -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = stringResource(MR.strings.label_sources),
                navigateUp = navigateUp,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        if (state.isEmpty) {
            EmptyScreen(
                stringRes = MR.strings.source_filter_empty_screen,
                modifier = Modifier.padding(contentPadding),
            )
            return@Scaffold
        }
        SourcesFilterContent(
            contentPadding = contentPadding,
            state = state,
            onClickLanguage = onClickLanguage,
            onClickSource = onClickSource,
        )
    }
}

@Composable
private fun SourcesFilterContent(
    contentPadding: PaddingValues,
    state: SourcesFilterViewModel.State.Success,
    onClickLanguage: (String) -> Unit,
    onClickSource: (Source) -> Unit,
) {
    FastScrollLazyColumn(
        contentPadding = contentPadding,
    ) {
        state.items.forEach { (language, sources) ->
            val enabled = language in state.enabledLanguages
            item(
                key = language,
                contentType = "source-filter-header",
            ) {
                SourcesFilterHeader(
                    modifier = Modifier.animateItem(),
                    language = language,
                    enabled = enabled,
                    onClickItem = onClickLanguage,
                )
            }
            if (enabled) {
                items(
                    items = sources,
                    key = { "source-filter-${it.key()}" },
                    contentType = { "source-filter-item" },
                ) { source ->
                    SourcesFilterItem(
                        modifier = Modifier.animateItem(),
                        source = source,
                        enabled = "${source.id}" !in state.disabledSources,
                        onClickItem = onClickSource,
                    )
                }
            }
        }
    }
}

@Composable
private fun SourcesFilterHeader(
    language: String,
    enabled: Boolean,
    onClickItem: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SwitchPreferenceWidget(
        modifier = modifier,
        title = LocaleHelper.getSourceDisplayName(language, LocalContext.current),
        checked = enabled,
        onCheckedChanged = { onClickItem(language) },
    )
}

@Composable
private fun SourcesFilterItem(
    source: Source,
    enabled: Boolean,
    onClickItem: (Source) -> Unit,
    modifier: Modifier = Modifier,
) {
    CheckboxPreference(
        modifier = modifier,
        title = source.name,
        checked = enabled,
        onCheckedChange = { onClickItem(source) },
        checkboxLocation = CheckboxLocation.End,
        startAction = { SourceIcon(source = source, modifier = Modifier.size(40.dp)) },
    )
}
