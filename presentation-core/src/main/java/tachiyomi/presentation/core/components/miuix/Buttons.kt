package tachiyomi.presentation.core.components.miuix

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import androidx.compose.material3.LocalTextStyle as MaterialLocalTextStyle
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonColors as MiuixButtonColors

/**
 * Small contextual actions retain Miuix's 40dp visual button and reserve the 48dp platform touch
 * target around it. The outer slot has no drawing or click handler; the native Button owns both.
 */
@Composable
fun CompactButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: MiuixButtonColors = ButtonDefaults.buttonColors(),
    buttonModifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = modifier.heightIn(min = CompactButtonDefaults.TouchTargetHeight),
        contentAlignment = Alignment.Center,
    ) {
        MiuixButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            colors = colors,
            minHeight = ButtonDefaults.MinHeight,
            insideMargin = CompactButtonDefaults.InsideMargin,
        ) {
            ButtonContent(style = MiuixTheme.textStyles.body2, content = content)
        }
    }
}

object CompactButtonDefaults {
    val TouchTargetHeight = 48.dp
    val IconSize = 18.dp
    val InsideMargin = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
}

/**
 * Keeps existing slot-based labels while letting Miuix own the button's shape and interaction.
 * Material color values are accepted only at this migration boundary.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors? = null,
    elevation: ButtonElevation? = null,
    shape: Shape? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.InsideMargin,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val miuixColors = if (colors == null) {
        ButtonDefaults.buttonColorsPrimary()
    } else {
        ButtonDefaults.buttonColors(
            color = colors.containerColor,
            contentColor = colors.contentColor,
            disabledColor = colors.disabledContainerColor,
            disabledContentColor = colors.disabledContentColor,
        )
    }
    MiuixButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = miuixColors,
        insideMargin = contentPadding,
        minHeight = 48.dp,
        interactionSource = interactionSource,
    ) {
        ButtonContent(content = content)
    }
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.InsideMargin,
    content: @Composable RowScope.() -> Unit,
) {
    MiuixButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        minHeight = 48.dp,
        colors = if (LocalDialogButtonPrimary.current) {
            ButtonDefaults.buttonColorsPrimary()
        } else {
            ButtonDefaults.buttonColors()
        },
        insideMargin = contentPadding,
    ) {
        ButtonContent(content = content)
    }
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    MiuixButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        minHeight = 48.dp,
    ) {
        ButtonContent(content = content)
    }
}

@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors? = null,
    content: @Composable RowScope.() -> Unit,
) {
    MiuixButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        minHeight = 48.dp,
        colors = if (colors == null) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.buttonColors(
                color = colors.containerColor,
                contentColor = colors.contentColor,
                disabledColor = colors.disabledContainerColor,
                disabledContentColor = colors.disabledContentColor,
            )
        },
    ) {
        ButtonContent(content = content)
    }
}

@Composable
internal fun RowScope.ButtonContent(
    style: TextStyle = MiuixTheme.textStyles.button,
    content: @Composable RowScope.() -> Unit,
) {
    CompositionLocalProvider(
        MaterialLocalContentColor provides LocalContentColor.current,
        MaterialLocalTextStyle provides style,
    ) {
        content()
    }
}

/** Miuix supplies the floating control; the expandable label is application content. */
@Composable
fun SmallExtendedFloatingActionButton(
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MiuixTheme.colorScheme.onPrimary,
            MaterialLocalContentColor provides MiuixTheme.colorScheme.onPrimary,
            MaterialLocalTextStyle provides MiuixTheme.textStyles.button,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon()
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally(),
                ) {
                    androidx.compose.foundation.layout.Box(Modifier.padding(start = 12.dp)) {
                        text()
                    }
                }
            }
        }
    }
}
