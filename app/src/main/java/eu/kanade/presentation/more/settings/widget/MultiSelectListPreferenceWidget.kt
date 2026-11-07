package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.miuix.MiuixPopupSystemBars
import tachiyomi.presentation.core.components.selectionSheetHeight
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixScroll
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun <T> MultiSelectListPreferenceWidget(
    values: Set<T>,
    title: String,
    subtitle: String?,
    icon: ImageVector?,
    entries: Map<out T, String>,
    onValuesChange: (Set<T>) -> Unit,
) {
    var isDialogShown by remember { mutableStateOf(false) }

    TextPreferenceWidget(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onPreferenceClick = { isDialogShown = true },
    )

    if (isDialogShown) {
        MultiSelectListPreferenceDialog(
            title = title,
            values = values,
            entries = entries,
            onValuesChange = onValuesChange,
            onDismissRequest = { isDialogShown = false },
        )
    }
}

@Composable
fun <T> MultiSelectListPreferenceDialog(
    title: String?,
    values: Set<T>,
    entries: Map<out T, String>,
    onValuesChange: (Set<T>) -> Unit,
    onDismissRequest: () -> Unit,
    message: String? = null,
) {
    var show by remember { mutableStateOf(true) }
    val dismiss = { show = false }
    // Record only edits, so newly loaded options retain their persisted selection.
    val selectionChanges = remember { mutableStateMapOf<T, Boolean>() }
    WindowDialog(
        modifier = Modifier.selectionSheetHeight(),
        show = show,
        onDismissRequest = dismiss,
        onDismissFinished = onDismissRequest,
        title = title,
        summary = message,
    ) {
        MiuixPopupSystemBars()
        Column {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false).miuixScroll(),
                overscrollEffect = null,
            ) {
                entries.forEach { current ->
                    item {
                        val isSelected = selectionChanges[current.key] ?: (current.key in values)
                        CheckboxPreference(
                            title = current.value,
                            checked = isSelected,
                            enabled = show,
                            onCheckedChange = {
                                selectionChanges[current.key] = it
                            },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = stringResource(MR.strings.action_cancel),
                    onClick = dismiss,
                    enabled = show,
                    modifier = Modifier.weight(1f),
                    minHeight = 48.dp,
                )
                TextButton(
                    text = stringResource(MR.strings.action_ok),
                    modifier = Modifier.weight(1f),
                    minHeight = 48.dp,
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    enabled = show,
                    onClick = {
                        // Unknown entries remain selected until they can be displayed.
                        onValuesChange(
                            applyMultiSelectChanges(values, entries.keys, selectionChanges),
                        )
                        dismiss()
                    },
                )
            }
        }
    }
}

internal fun <T> applyMultiSelectChanges(
    values: Set<T>,
    availableEntries: Set<T>,
    changes: Map<T, Boolean>,
): Set<T> = values.toMutableSet().apply {
    availableEntries.forEach { key ->
        when (changes[key]) {
            true -> add(key)
            false -> remove(key)
            null -> Unit
        }
    }
}
