package eu.kanade.presentation.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.DatePicker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_cancel
import tachiyomi.i18n.action_ok
import tachiyomi.i18n.action_remove
import tachiyomi.i18n.chapters
import tachiyomi.i18n.completed
import tachiyomi.i18n.dropped
import tachiyomi.i18n.on_hold
import tachiyomi.i18n.plan_to_read
import tachiyomi.i18n.reading
import tachiyomi.i18n.repeating
import tachiyomi.i18n.score
import tachiyomi.i18n.status
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.WheelNumberPicker
import tachiyomi.presentation.core.components.WheelTextPicker
import tachiyomi.presentation.core.components.material.AlertDialogContent
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.miuix.Surface
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.components.miuix.TextButton
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixVerticalScroll
import top.yukonga.miuix.kmp.preference.RadioButtonPreference

@Composable
fun TrackStatusSelector(
    selection: Long,
    onSelectionChange: (Long) -> Unit,
    selections: Map<Long, StringResource?>,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    BaseSelector(
        title = stringResource(MR.strings.status),
        content = {
            val state = rememberLazyListState()
            ScrollbarLazyColumn(state = state, modifier = Modifier.selectableGroup()) {
                selections.forEach { (key, value) ->
                    val isSelected = selection == key
                    item(key = key) {
                        RadioButtonPreference(
                            title = value?.let { stringResource(it) }.orEmpty(),
                            selected = isSelected,
                            onClick = { onSelectionChange(key) },
                        )
                    }
                }
            }
        },
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest,
    )
}

@Composable
fun TrackChapterSelector(
    selection: Int,
    onSelectionChange: (Int) -> Unit,
    range: Iterable<Int>,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val chapters = remember(range) { range.toList() }
    BaseSelector(
        title = stringResource(MR.strings.chapters),
        content = {
            WheelNumberPicker(
                items = chapters,
                modifier = Modifier.align(Alignment.Center),
                startIndex = chapters.indexOfFirst { it >= selection }
                    .takeIf { it >= 0 } ?: chapters.lastIndex.coerceAtLeast(0),
                onSelectionChanged = { onSelectionChange(chapters[it]) },
            )
        },
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest,
    )
}

@Composable
fun TrackScoreSelector(
    selection: String,
    onSelectionChange: (String) -> Unit,
    selections: List<String>,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    BaseSelector(
        title = stringResource(MR.strings.score),
        content = {
            WheelTextPicker(
                items = selections,
                modifier = Modifier.align(Alignment.Center),
                startIndex = selections.indexOf(selection).takeIf { it >= 0 } ?: (selections.size / 2),
                onSelectionChanged = { onSelectionChange(selections[it]) },
            )
        },
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest,
    )
}

@Composable
fun TrackDateSelector(
    title: String,
    initialSelectedDateMillis: Long,
    selectableDates: SelectableDates,
    onConfirm: (Long) -> Unit,
    onRemove: (() -> Unit)?,
    onDismissRequest: () -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        selectableDates = selectableDates,
    )
    val selectedDate = pickerState.selectedDateMillis
    val canConfirm = selectedDate != null && selectableDates.isSelectableDate(selectedDate)
    AlertDialogContent(
        title = { Text(text = title) },
        content = {
            Column {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .miuixVerticalScroll(rememberScrollState()),
                ) {
                    DatePicker(
                        state = pickerState,
                        title = null,
                        headline = null,
                        showModeToggle = false,
                    )
                }

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (onRemove != null) {
                        TextButton(onClick = onRemove) {
                            Text(text = stringResource(MR.strings.action_remove))
                        }
                    }
                    TextButton(onClick = onDismissRequest) {
                        Text(text = stringResource(MR.strings.action_cancel))
                    }
                    TextButton(
                        onClick = { selectedDate?.takeIf(selectableDates::isSelectableDate)?.let(onConfirm) },
                        enabled = canConfirm,
                    ) {
                        Text(text = stringResource(MR.strings.action_ok))
                    }
                }
            }
        },
    )
}

@Composable
private fun BaseSelector(
    title: String,
    content: @Composable BoxScope.() -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    thirdButton: (@Composable () -> Unit)? = null,
) {
    AlertDialogContent(
        title = { Text(text = title) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                content = content,
            )
        },
        buttons = {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (thirdButton != null) {
                    thirdButton()
                }
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(MR.strings.action_cancel))
                }
                TextButton(onClick = onConfirm) {
                    Text(text = stringResource(MR.strings.action_ok))
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun TrackStatusSelectorPreviews() {
    TachiyomiPreviewTheme {
        Surface {
            TrackStatusSelector(
                selection = 1,
                onSelectionChange = {},
                selections = mapOf(
                    // Anilist values
                    1L to MR.strings.reading,
                    2L to MR.strings.plan_to_read,
                    3L to MR.strings.completed,
                    4L to MR.strings.on_hold,
                    5L to MR.strings.dropped,
                    6L to MR.strings.repeating,
                ),
                onConfirm = {},
                onDismissRequest = {},
            )
        }
    }
}
