package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.i18n.unknown_error
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.TextFieldColors
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import androidx.compose.material3.LocalTextStyle as MaterialLocalTextStyle
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField

/**
 * Existing settings use composable labels and supporting text. Those slots sit around the public
 * Miuix field; editing, focus, selection, keyboard actions and field chrome remain framework owned.
 * [modifier] constrains the complete field and supporting content. [fieldModifier] targets the
 * editable node for focus, autofill and other editing semantics.
 */
@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
) {
    TextFieldSlots(modifier, label, supportingText, enabled, isError) {
        MiuixTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier.fillMaxWidth().fieldSemantics(labelText, isError, errorMessage),
            label = labelText,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            colors = fieldColors(colors, isError),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
        )
    }
}

@Composable
fun OutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
) {
    TextFieldSlots(modifier, label, supportingText, enabled, isError) {
        MiuixTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier.fillMaxWidth().fieldSemantics(labelText, isError, errorMessage),
            label = labelText,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            colors = fieldColors(colors, isError),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
        )
    }
}

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
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
) {
    TextFieldSlots(modifier, label, supportingText, enabled, isError) {
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
