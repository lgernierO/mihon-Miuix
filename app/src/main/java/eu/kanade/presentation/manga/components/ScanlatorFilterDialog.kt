package eu.kanade.presentation.manga.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.components.miuix.TextButton
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixScroll
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.CheckboxPreference

/** A secondary page of the chapter settings sheet, retaining its window and viewport. */
@Composable
fun ScanlatorFilterPage(
    availableScanlators: Set<String>,
    excludedScanlators: Set<String>,
    onDismissRequest: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
) {
    val sortedScanlators = remember(availableScanlators) {
        availableScanlators.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
    }
    var selected by rememberSaveable { mutableStateOf(excludedScanlators.toList()) }
    Column(Modifier.fillMaxWidth()) {
        SmallTitle(text = stringResource(MR.strings.exclude_scanlators))
        Card(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().miuixScroll(),
                overscrollEffect = null,
            ) {
                if (sortedScanlators.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(MR.strings.no_scanlators_found),
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
                items(sortedScanlators, key = { it }) { scanlator ->
                    CheckboxPreference(
                        title = scanlator,
                        checked = scanlator in selected,
                        onCheckedChange = { checked ->
                            selected = if (checked) {
                                (selected + scanlator).distinct()
                            } else {
                                selected - scanlator
                            }
                        },
                    )
                }
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (sortedScanlators.isNotEmpty()) {
                TextButton(
                    onClick = {
                        selected = if (selected.isEmpty()) availableScanlators.toList() else emptyList()
                    },
                ) {
                    Text(
                        stringResource(
                            if (selected.isEmpty()) MR.strings.action_select_all else MR.strings.action_reset,
                        ),
                    )
                }
            }
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(MR.strings.action_cancel))
            }
            if (sortedScanlators.isNotEmpty()) {
                TextButton(
                    onClick = {
                        onConfirm(selected.toSet())
                        onDismissRequest()
                    },
                ) {
                    Text(stringResource(MR.strings.action_ok))
                }
            }
        }
    }
}
