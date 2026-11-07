package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CheckboxDefaults
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import top.yukonga.miuix.kmp.basic.Checkbox as MiuixCheckbox
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.IconButtonDefaults as MiuixIconButtonDefaults

@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    MiuixCheckbox(
        state = if (checked) ToggleableState.On else ToggleableState.Off,
        onClick = onCheckedChange?.let { callback -> { callback(!checked) } },
        modifier = if (onCheckedChange == null) modifier else modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        enabled = enabled,
    )
}

@Composable
fun TriStateCheckbox(
    state: ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    MiuixCheckbox(
        state = state,
        onClick = onClick,
        modifier = if (onClick == null) modifier else modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        colors = CheckboxDefaults.checkboxColors(),
        enabled = enabled,
    )
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    contentModifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val contentColor = if (enabled) LocalContentColor.current else MiuixTheme.colorScheme.disabledOnSurface
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        MaterialLocalContentColor provides contentColor,
    ) {
        if (onLongClick == null) {
            MiuixIconButton(
                onClick = onClick,
                modifier = modifier.buttonStateSemantics(enabled),
                enabled = enabled,
                minWidth = 48.dp,
                minHeight = 48.dp,
            ) {
                Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                    content()
                }
            }
        } else {
            Card(
                modifier = modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .buttonStateSemantics(enabled),
                cornerRadius = MiuixIconButtonDefaults.CornerRadius,
                colors = CardDefaults.defaultColors(color = Color.Transparent, contentColor = contentColor),
                onClick = onClick.takeIf { enabled },
                onLongPress = onLongClick.takeIf { enabled },
                showIndication = true,
            ) {
                Box(
                    modifier = contentModifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun FilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors? = null,
    shape: Shape? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    val background = if (colors == null) {
        if (enabled) MiuixTheme.colorScheme.secondaryVariant else MiuixTheme.colorScheme.disabledSecondaryVariant
    } else {
        if (enabled) colors.containerColor else colors.disabledContainerColor
    }
    val foreground = if (colors == null) {
        if (enabled) MiuixTheme.colorScheme.onSecondaryVariant else MiuixTheme.colorScheme.disabledOnSecondaryVariant
    } else {
        if (enabled) colors.contentColor else colors.disabledContentColor
    }
    MiuixIconButton(
        onClick = onClick,
        modifier = modifier.buttonStateSemantics(enabled),
        enabled = enabled,
        backgroundColor = background.takeIf { it != Color.Unspecified } ?: Color.Transparent,
        minWidth = 48.dp,
        minHeight = 48.dp,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides foreground,
            MaterialLocalContentColor provides foreground,
            content = content,
        )
    }
}

private fun Modifier.buttonStateSemantics(enabled: Boolean): Modifier = semantics {
    role = Role.Button
    if (!enabled) disabled()
}
