package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.LocalPreferenceHighlighted
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_cancel
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.miuix.MiuixPopupSystemBars
import tachiyomi.presentation.core.components.selectionSheetHeight
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun <T> ListPreferenceWidget(
    value: T,
    title: String,
    subtitle: String?,
    icon: ImageVector?,
    entries: Map<out T, String>,
    onValueChange: (T) -> Unit,
) {
    val options = remember(entries) { entries.entries.toList() }
    WindowDropdownPreference(
        modifier = Modifier.highlightBackground(LocalPreferenceHighlighted.current),
        items = options.map { it.value },
        selectedIndex = options.indexOfFirst { it.key == value },
        title = title,
        summary = subtitle,
        showValue = false,
        startAction = icon?.let { { Icon(imageVector = it, contentDescription = null) } },
        onSelectedIndexChange = { index -> options.getOrNull(index)?.let { onValueChange(it.key) } },
    )
}

@Composable
fun <T> ListPreferenceDialog(
    title: String?,
    value: T,
    entries: Map<out T, String>,
    onValueChange: (T) -> Unit,
    onDismissRequest: () -> Unit,
    message: String? = null,
) {
    var show by remember { mutableStateOf(true) }
    val dismiss = { show = false }
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
            Box(modifier = Modifier.weight(1f, fill = false)) {
                val state = rememberLazyListState()
                ScrollbarLazyColumn(
                    modifier = Modifier.selectableGroup(),
                    state = state,
                ) {
                    entries.forEach { current ->
                        val isSelected = value == current.key
                        item {
                            RadioButtonPreference(
                                title = current.value,
                                selected = isSelected,
                                enabled = show,
                                onClick = {
                                    if (!isSelected) {
                                        onValueChange(current.key)
                                    }
                                    dismiss()
                                },
                            )
                        }
                    }
                }
            }
            TextButton(
                text = stringResource(MR.strings.action_cancel),
                onClick = dismiss,
                enabled = show,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                minHeight = 48.dp,
            )
        }
    }
}

@Composable
internal fun PreferenceDialogMessage(message: String) {
    Text(text = message, modifier = Modifier.padding(bottom = 8.dp))
}
