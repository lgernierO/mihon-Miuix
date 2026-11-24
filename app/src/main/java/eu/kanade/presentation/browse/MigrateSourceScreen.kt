package eu.kanade.presentation.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.kanade.domain.source.interactor.SetMigrateSorting
import eu.kanade.presentation.browse.components.SourceIcon
import eu.kanade.tachiyomi.ui.browse.migration.sources.MigrateSourceViewModel
import eu.kanade.tachiyomi.util.system.LocaleHelper
import eu.kanade.tachiyomi.util.system.copyToClipboard
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_asc
import tachiyomi.i18n.action_desc
import tachiyomi.i18n.action_sort_alpha
import tachiyomi.i18n.action_sort_count
import tachiyomi.i18n.information_empty_library
import tachiyomi.i18n.migration_selection_prompt
import tachiyomi.i18n.not_installed
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.components.miuix.CompactActionIcon
import tachiyomi.presentation.core.components.miuix.CompactIconButton
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.plus
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.NumeralSystemConversion
import top.yukonga.miuix.kmp.icon.glass.PageDown
import top.yukonga.miuix.kmp.icon.glass.PageUp
import top.yukonga.miuix.kmp.icon.glass.Sort
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MigrateSourceScreen(
    state: MigrateSourceViewModel.State,
    contentPadding: PaddingValues,
    onClickItem: (Source) -> Unit,
) {
    val context = LocalContext.current
    when {
        state.isLoading -> LoadingScreen(Modifier.padding(contentPadding))
        state.isEmpty -> EmptyScreen(
            stringRes = MR.strings.information_empty_library,
            modifier = Modifier.padding(contentPadding),
        )
        else ->
            MigrateSourceList(
                list = state.items,
                contentPadding = contentPadding,
                onClickItem = onClickItem,
                onLongClickItem = { source ->
                    val sourceId = source.id.toString()
                    context.copyToClipboard(sourceId, sourceId)
                },
            )
    }
}

@Composable
private fun MigrateSourceList(
    list: List<Pair<Source, Long>>,
    contentPadding: PaddingValues,
    onClickItem: (Source) -> Unit,
    onLongClickItem: (Source) -> Unit,
) {
    ScrollbarLazyColumn(
        contentPadding = contentPadding + topSmallPaddingValues,
    ) {
        items(
            items = list,
            key = { (source, _) -> "migrate-${source.id}" },
        ) { (source, count) ->
            MigrateSourceItem(
                modifier = Modifier.animateItem(),
                source = source,
                count = count,
                onClickItem = { onClickItem(source) },
                onLongClickItem = { onLongClickItem(source) },
            )
        }
    }
}

/** Kept with the browse tabs so it remains reachable while the source list passes underneath. */
@Composable
fun MigrateSourceSortingHeader(
    sortingMode: SetMigrateSorting.Mode,
    onToggleSortingMode: () -> Unit,
    sortingDirection: SetMigrateSorting.Direction,
    onToggleSortingDirection: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(MR.strings.migration_selection_prompt),
            modifier = Modifier.weight(1f),
            style = MiuixTheme.textStyles.subtitle,
        )

        CompactIconButton(onClick = onToggleSortingMode) {
            when (sortingMode) {
                SetMigrateSorting.Mode.ALPHABETICAL -> CompactActionIcon(
                    imageVector = MiuixIcons.Glass.Sort,
                    contentDescription = stringResource(MR.strings.action_sort_alpha),
                )
                SetMigrateSorting.Mode.TOTAL -> CompactActionIcon(
                    imageVector = MiuixIcons.Glass.NumeralSystemConversion,
                    contentDescription = stringResource(MR.strings.action_sort_count),
                )
            }
        }
        CompactIconButton(onClick = onToggleSortingDirection) {
            when (sortingDirection) {
                SetMigrateSorting.Direction.ASCENDING -> CompactActionIcon(
                    imageVector = MiuixIcons.Glass.PageUp,
                    contentDescription = stringResource(MR.strings.action_asc),
                )
                SetMigrateSorting.Direction.DESCENDING -> CompactActionIcon(
                    imageVector = MiuixIcons.Glass.PageDown,
                    contentDescription = stringResource(MR.strings.action_desc),
                )
            }
        }
    }
}

@Composable
private fun MigrateSourceItem(
    source: Source,
    count: Long,
    onClickItem: () -> Unit,
    onLongClickItem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val summary = listOfNotNull(
        LocaleHelper.getSourceDisplayName(source.lang, context).takeIf { source.lang.isNotEmpty() },
        stringResource(MR.strings.not_installed).takeIf { source.isStub },
    ).joinToString(" · ").ifEmpty { null }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClickItem,
        onLongPress = onLongClickItem,
        showIndication = true,
    ) {
        BasicComponent(
            title = source.name.ifBlank { source.id.toString() },
            summary = summary,
            startAction = { SourceIcon(source = source, modifier = Modifier.size(40.dp)) },
            endActions = {
                Badge(
                    containerColor = MiuixTheme.colorScheme.secondaryVariant,
                    contentColor = MiuixTheme.colorScheme.onSecondaryVariant,
                ) {
                    top.yukonga.miuix.kmp.basic.Text(text = "$count")
                }
            },
        )
    }
}
