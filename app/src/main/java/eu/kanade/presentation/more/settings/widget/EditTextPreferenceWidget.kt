package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.roundedfilled.Cancel
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.miuix.MiuixPopupSystemBars
import tachiyomi.presentation.core.components.selectionSheetHeight
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixVerticalScroll
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun EditTextPreferenceWidget(
    title: String,
    subtitle: String?,
    icon: ImageVector?,
    value: String,
    onConfirm: suspend (String) -> Boolean,
) {
    var isDialogShown by remember { mutableStateOf(false) }

    TextPreferenceWidget(
        title = title,
        subtitle = subtitle?.format(value),
        icon = icon,
        onPreferenceClick = { isDialogShown = true },
    )

    if (isDialogShown) {
        val scope = rememberCoroutineScope()
        var show by remember { mutableStateOf(true) }
        var confirmJob by remember { mutableStateOf<Job?>(null) }
        val dismiss = {
            confirmJob?.cancel()
            show = false
        }
        var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(value))
        }
        var processing by remember { mutableStateOf(false) }
        val inputEmpty = textFieldValue.text.isBlank()
        val requiredMessage = stringResource(MR.strings.information_required_plain)
        WindowDialog(
            modifier = Modifier.selectionSheetHeight(),
            show = show,
            onDismissRequest = dismiss,
            onDismissFinished = { isDialogShown = false },
            title = title,
        ) {
            MiuixPopupSystemBars()
            Column {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .miuixVerticalScroll(rememberScrollState()),
                ) {
                    TextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        label = title,
                        enabled = show && !processing,
                        trailingIcon = {
                            if (!inputEmpty) {
                                IconButton(
                                    enabled = show && !processing,
                                    onClick = { textFieldValue = TextFieldValue("") },
                                    minWidth = 48.dp,
                                    minHeight = 48.dp,
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.RoundedFilled.Cancel,
                                        contentDescription = stringResource(MR.strings.action_reset),
                                    )
                                }
                            }
                        },
                        colors = if (inputEmpty) {
                            TextFieldDefaults.textFieldColors(
                                labelColor = MiuixTheme.colorScheme.error,
                                borderColor = MiuixTheme.colorScheme.error,
                            )
                        } else {
                            TextFieldDefaults.textFieldColors()
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().semantics {
                            if (inputEmpty) error(requiredMessage)
                        },
                    )
                    if (inputEmpty) {
                        Text(
                            text = requiredMessage,
                            color = MiuixTheme.colorScheme.error,
                            style = MiuixTheme.textStyles.body2,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
                        enabled = show && !processing && textFieldValue.text != value && !inputEmpty,
                        onClick = {
                            processing = true
                            val submittedValue = textFieldValue.text
                            confirmJob = scope.launch {
                                try {
                                    if (onConfirm(submittedValue)) {
                                        show = false
                                    }
                                } finally {
                                    processing = false
                                    confirmJob = null
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
