package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.miuix.ButtonContent
import tachiyomi.presentation.core.components.miuix.LocalDialogButtonPrimary
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape? = null,
    border: BorderStroke? = null,
    colors: ButtonColors = if (LocalDialogButtonPrimary.current) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.secondaryButtonColors()
    },
    contentPadding: PaddingValues = MiuixButtonDefaults.InsideMargin,
    contentModifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) = Button(
    onClick = onClick,
    modifier = modifier,
    onLongClick = onLongClick,
    enabled = enabled,
    interactionSource = interactionSource,
    elevation = elevation,
    shape = shape,
    border = border,
    colors = colors,
    contentPadding = contentPadding,
    contentModifier = contentModifier,
    content = content,
)

/**
 * The legacy name remains for callers with long-press actions. Rendering and gestures are
 * delegated to Miuix: Button for ordinary actions, interactive Card for the long-press variant.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape? = null,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = MiuixButtonDefaults.InsideMargin,
    contentModifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    if (onLongClick == null) {
        MiuixButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            minHeight = 48.dp,
            colors = MiuixButtonDefaults.buttonColors(
                color = colors.containerColor,
                contentColor = colors.contentColor,
                disabledColor = colors.disabledContainerColor,
                disabledContentColor = colors.disabledContentColor,
            ),
            insideMargin = contentPadding,
            interactionSource = interactionSource,
        ) {
            if (contentModifier == Modifier) {
                ButtonContent(content = content)
            } else {
                Row(
                    modifier = contentModifier,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ButtonContent(content = content)
                }
            }
        }
    } else {
        // The public Miuix Button has no long-click callback; interactive Card does.
        val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
        val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
        Card(
            modifier = modifier.semantics {
                role = Role.Button
                if (!enabled) disabled()
            },
            colors = CardDefaults.defaultColors(color = containerColor, contentColor = contentColor),
            onClick = onClick.takeIf { enabled },
            onLongPress = onLongClick.takeIf { enabled },
            showIndication = true,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                Row(
                    modifier = contentModifier
                        .align(Alignment.CenterHorizontally)
                        .defaultMinSize(minWidth = MiuixButtonDefaults.MinWidth, minHeight = 48.dp)
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ButtonContent(content = content)
                }
            }
        }
    }
}

object ButtonDefaults {
    @Composable
    fun buttonColors(
        containerColor: androidx.compose.ui.graphics.Color = MiuixTheme.colorScheme.primary,
        contentColor: androidx.compose.ui.graphics.Color = MiuixTheme.colorScheme.onPrimary,
        disabledContainerColor: androidx.compose.ui.graphics.Color = MiuixTheme.colorScheme.disabledPrimaryButton,
        disabledContentColor: androidx.compose.ui.graphics.Color = MiuixTheme.colorScheme.disabledOnPrimaryButton,
    ): ButtonColors = ButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    @Composable
    fun secondaryButtonColors(): ButtonColors = ButtonColors(
        containerColor = MiuixTheme.colorScheme.secondaryVariant,
        contentColor = MiuixTheme.colorScheme.onSecondaryVariant,
        disabledContainerColor = MiuixTheme.colorScheme.disabledSecondaryVariant,
        disabledContentColor = MiuixTheme.colorScheme.disabledOnSecondaryVariant,
    )

    @Composable
    fun buttonElevation(
        defaultElevation: Dp = 0.dp,
        pressedElevation: Dp = 0.dp,
        focusedElevation: Dp = 0.dp,
        hoveredElevation: Dp = 0.dp,
        disabledElevation: Dp = 0.dp,
    ): ButtonElevation = ButtonElevation(
        defaultElevation,
        pressedElevation,
        focusedElevation,
        hoveredElevation,
        disabledElevation,
    )
}

/** Accepted by existing source callers; Miuix supplies the visual elevation. */
@Immutable
@ConsistentCopyVisibility
data class ButtonElevation internal constructor(
    val defaultElevation: Dp,
    val pressedElevation: Dp,
    val focusedElevation: Dp,
    val hoveredElevation: Dp,
    val disabledElevation: Dp,
)

@Immutable
@ConsistentCopyVisibility
data class ButtonColors internal constructor(
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val disabledContainerColor: androidx.compose.ui.graphics.Color,
    val disabledContentColor: androidx.compose.ui.graphics.Color,
)
