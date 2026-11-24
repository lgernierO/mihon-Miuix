package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_cancel
import tachiyomi.i18n.action_ok
import tachiyomi.i18n.action_reset
import tachiyomi.i18n.information_required_plain
import tachiyomi.presentation.core.components.miuix.AppGlassDialog
import tachiyomi.presentation.core.components.miuix.CompactActionIcon
import tachiyomi.presentation.core.components.miuix.MiuixPopupSystemBars
import tachiyomi.presentation.core.components.miuix.TextButton
import tachiyomi.presentation.core.components.selectionSheetHeight
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixVerticalScroll
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.glass.GlassTextField
import top.yukonga.miuix.kmp.glass.GlassTextFieldDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Clear
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
        val textFieldValue = rememberTextFieldState(value)
        var processing by remember { mutableStateOf(false) }
        val inputEmpty = textFieldValue.text.isBlank()
        val requiredMessage = stringResource(MR.strings.information_required_plain)
        AppGlassDialog(
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
                    GlassTextField(
                        state = textFieldValue,
                        label = title,
                        enabled = show && !processing,
                        trailingIcon = {
                            if (!inputEmpty) {
                                IconButton(
                                    enabled = show && !processing,
                                    onClick = { textFieldValue.clearText() },
                                    minWidth = 48.dp,
                                    minHeight = 48.dp,
                                ) {
                                    CompactActionIcon(
                                        imageVector = MiuixIcons.Glass.Clear,
                                        contentDescription = stringResource(MR.strings.action_reset),
                                        tint = if (show && !processing) {
                                            MiuixTheme.colorScheme.onSecondaryContainer
                                        } else {
                                            MiuixTheme.colorScheme.disabledOnSecondaryVariant
                                        },
                                    )
                                }
                            }
                        },
                        colors = if (inputEmpty) {
                            GlassTextFieldDefaults.textFieldColors(
                                labelColor = MiuixTheme.colorScheme.error,
                                borderColor = MiuixTheme.colorScheme.error,
                            )
                        } else {
                            GlassTextFieldDefaults.textFieldColors()
                        },
                        lineLimits = TextFieldLineLimits.SingleLine,
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
                        enabled = show && !processing && textFieldValue.text.toString() != value && !inputEmpty,
                        onClick = {
                            processing = true
                            val submittedValue = textFieldValue.text.toString()
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
