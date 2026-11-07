package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import androidx.compose.material3.LocalTextStyle as MaterialLocalTextStyle

/** Lets the shared button adapter give a dialog's confirmation the primary Miuix role. */
val LocalDialogButtonPrimary = compositionLocalOf { false }

/**
 * Keeps the application's existing title/body/action slots while Miuix owns the dialog window,
 * adaptive placement, shape, insets and predictive-back interaction.
 */
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    var show by remember { mutableStateOf(true) }
    // Miuix exposes a shared back/outside callback. The application's locked progress dialogs
    // disable both; ordinary dialogs enable both.
    val implicitDismiss = properties.dismissOnBackPress && properties.dismissOnClickOutside

    WindowDialog(
        show = show,
        modifier = modifier,
        onDismissRequest = if (implicitDismiss) {
            { show = false }
        } else {
            null
        },
        onDismissFinished = onDismissRequest,
    ) {
        MiuixPopupSystemBars()
        Column(Modifier.fillMaxWidth()) {
            icon?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 12.dp),
                ) {
                    DialogSlot(
                        color = MiuixTheme.colorScheme.primary,
                        style = MiuixTheme.textStyles.main,
                        content = it,
                    )
                }
            }
            title?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    DialogSlot(
                        color = MiuixTheme.colorScheme.onBackground,
                        style = MiuixTheme.textStyles.title4.copy(
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                        ),
                        content = it,
                    )
                }
            }
            text?.let {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                ) {
                    DialogSlot(
                        color = MiuixTheme.colorScheme.onSurfaceSecondary,
                        style = MiuixTheme.textStyles.body1,
                        content = it,
                    )
                }
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                dismissButton?.let {
                    Box(Modifier.padding(end = 8.dp)) {
                        CompositionLocalProvider(LocalDialogButtonPrimary provides false) {
                            DialogSlot(
                                color = MiuixTheme.colorScheme.onSecondaryVariant,
                                style = MiuixTheme.textStyles.button,
                                content = it,
                            )
                        }
                    }
                }
                CompositionLocalProvider(LocalDialogButtonPrimary provides true) {
                    DialogSlot(
                        color = MiuixTheme.colorScheme.onPrimary,
                        style = MiuixTheme.textStyles.button,
                        content = confirmButton,
                    )
                }
            }
        }
    }
}

/** Content slots still used by third-party code receive the same semantic colors and type. */
@Composable
private fun DialogSlot(
    color: Color,
    style: TextStyle,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalContentColor provides color,
        MaterialLocalContentColor provides color,
        MaterialLocalTextStyle provides style,
        content = content,
    )
}
