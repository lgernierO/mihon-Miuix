package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import androidx.compose.material3.LocalTextStyle as MaterialLocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.i18n.unknown_error
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.TextFieldColors
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.glass.GlassTextField
import top.yukonga.miuix.kmp.glass.GlassTextFieldDefaults
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Existing settings use composable labels and supporting text. Those slots sit around the public
 * Miuix field; editing, focus, selection, keyboard actions and field chrome remain framework owned.
 * [modifier] constrains the complete field and supporting content. [fieldModifier] targets the
 * editable node for focus, autofill and other editing semantics.
 */
@Composable
fun OutlinedTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MiuixTheme.textStyles.main,
    labelText: String = "",
    label: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    interactionSource: MutableInteractionSource? = null,
    colors: TextFieldColors = if (LocalGlassDialog.current) GlassTextFieldDefaults.textFieldColors() else TextFieldDefaults.textFieldColors(),
) {
    TextFieldSlots(modifier, label, supportingText, enabled, isError) {
        if (LocalGlassDialog.current) {
            GlassTextField(
                state = state,
                modifier = fieldModifier.fillMaxWidth().fieldSemantics(labelText, isError, errorMessage),
                label = labelText,
                enabled = enabled,
                readOnly = readOnly,
                textStyle = textStyle,
                colors = fieldColors(colors, isError),
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                keyboardOptions = keyboardOptions,
                onKeyboardAction = onKeyboardAction,
                lineLimits = lineLimits,
                inputTransformation = inputTransformation,
                outputTransformation = outputTransformation,
                interactionSource = interactionSource,
            )
        } else {
            MiuixTextField(
                state = state,
                modifier = fieldModifier.fillMaxWidth().fieldSemantics(labelText, isError, errorMessage),
                label = labelText,
                enabled = enabled,
                readOnly = readOnly,
                textStyle = textStyle,
                colors = fieldColors(colors, isError),
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                keyboardOptions = keyboardOptions,
                onKeyboardAction = onKeyboardAction,
                lineLimits = lineLimits,
                inputTransformation = inputTransformation,
                outputTransformation = outputTransformation,
                interactionSource = interactionSource,
            )
        }
    }
}

@Composable
private fun TextFieldSlots(
    modifier: Modifier,
    label: (@Composable () -> Unit)?,
    supportingText: (@Composable () -> Unit)?,
    enabled: Boolean,
    isError: Boolean,
    field: @Composable () -> Unit,
) {
    val supportingColor = when {
        !enabled -> MiuixTheme.colorScheme.disabledOnSecondaryVariant
        isError -> MiuixTheme.colorScheme.error
        else -> MiuixTheme.colorScheme.onSurfaceVariantSummary
    }
    Column(modifier = modifier) {
        label?.let {
            CompositionLocalProvider(
                LocalContentColor provides supportingColor,
                MaterialLocalContentColor provides supportingColor,
                MaterialLocalTextStyle provides MiuixTheme.textStyles.body2,
                content = it,
            )
            Spacer(Modifier.height(6.dp))
        }
        field()
        supportingText?.let {
            Spacer(Modifier.height(6.dp))
            CompositionLocalProvider(
                LocalContentColor provides supportingColor,
                MaterialLocalContentColor provides supportingColor,
                MaterialLocalTextStyle provides MiuixTheme.textStyles.footnote1,
                content = it,
            )
        }
    }
}

@Composable
private fun fieldColors(colors: TextFieldColors, isError: Boolean): TextFieldColors = if (isError) {
    colors.copy(
        borderColor = MiuixTheme.colorScheme.error,
        labelColor = MiuixTheme.colorScheme.error,
    )
} else {
    colors
}

@Composable
private fun Modifier.fieldSemantics(
    label: String,
    isError: Boolean,
    message: String?,
): Modifier {
    val errorText = message ?: stringResource(MR.strings.unknown_error)
    return semantics {
        if (label.isNotBlank()) contentDescription = label
        if (isError) error(errorText)
    }
}

/** Keep externally owned query/filter values synchronized without recreating the editor or selection. */
@Composable
fun rememberSyncedTextFieldState(value: String, onValueChange: (String) -> Unit): TextFieldState {
    val state = rememberTextFieldState(value)
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    LaunchedEffect(value) {
        if (state.text.toString() != value) state.setTextAndPlaceCursorAtEnd(value)
    }
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }.collect { text ->
            if (text != currentValue) currentOnValueChange(text)
        }
    }
    return state
}
