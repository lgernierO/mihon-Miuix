package tachiyomi.presentation.core.components.miuix

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tachiyomi.i18n.MR
import tachiyomi.i18n.unknown_error
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.glass.GlassTextFieldDefaults
import top.yukonga.miuix.kmp.squircle.squircleBackground
import top.yukonga.miuix.kmp.squircle.squircleBorder
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Application adapter: Miuix field chrome around Foundation's secure password editor. */
@Composable
fun OutlinedSecureTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labelText: String = "",
    trailingIcon: @Composable (() -> Unit)? = null,
    textObfuscationMode: TextObfuscationMode = TextObfuscationMode.Hidden,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Password,
    ),
    onKeyboardAction: KeyboardActionHandler? = null,
    isError: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val floatingLabel = labelText.isNotEmpty() && state.text.isNotEmpty()
    val insideMargin = TextFieldDefaults.InsideMargin
    val defaultColors = if (LocalGlassDialog.current) GlassTextFieldDefaults.textFieldColors() else TextFieldDefaults.textFieldColors()
    val colors = if (isError) {
        defaultColors.copy(borderColor = MiuixTheme.colorScheme.error, labelColor = MiuixTheme.colorScheme.error)
    } else {
        defaultColors
    }
    // Match the pinned Miuix TextField's focus border and inline/floating label animation.
    val borderWidth = animateDpAsState(if (focused) 2.dp else 0.dp, label = "secureFieldBorder")
    val borderColor = animateColorAsState(
        if (focused) colors.borderColor else colors.backgroundColor,
        label = "secureFieldBorderColor",
    )
    val labelOffset = animateDpAsState(
        if (floatingLabel) -insideMargin.height / 2 else 0.dp,
        label = "secureFieldLabelOffset",
    )
    val labelSize by animateDpAsState(if (floatingLabel) 10.dp else 17.dp, label = "secureFieldLabelSize")
    val contentColor = LocalContentColor.current
    val textStyle = MiuixTheme.textStyles.main
    val errorText = stringResource(MR.strings.unknown_error)

    BasicSecureTextField(
        state = state,
        modifier = modifier.fillMaxWidth().semantics {
            if (labelText.isNotEmpty()) contentDescription = labelText
            if (isError) error(errorText)
        },
        enabled = enabled,
        textStyle = textStyle.copy(color = textStyle.color.takeOrElse { contentColor }),
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        interactionSource = interactionSource,
        textObfuscationMode = textObfuscationMode,
        cursorBrush = SolidColor(colors.borderColor),
        decorator = TextFieldDecorator { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .squircleBackground(colors.backgroundColor, TextFieldDefaults.CornerRadius)
                    .squircleBorder(
                        width = { borderWidth.value },
                        color = { borderColor.value },
                        cornerRadius = TextFieldDefaults.CornerRadius,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = insideMargin.width,
                            end = if (trailingIcon == null) insideMargin.width else 0.dp,
                            top = insideMargin.height,
                            bottom = insideMargin.height,
                        ),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (labelText.isNotEmpty()) {
                        Text(
                            text = labelText,
                            fontSize = labelSize.value.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.labelColor,
                            modifier = Modifier.offset { IntOffset(0, labelOffset.value.roundToPx()) },
                        )
                    }
                    Box(
                        modifier = Modifier.offset(y = if (floatingLabel) insideMargin.height / 2 else 0.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        innerTextField()
                    }
                }
                trailingIcon?.invoke()
            }
        },
    )
}
