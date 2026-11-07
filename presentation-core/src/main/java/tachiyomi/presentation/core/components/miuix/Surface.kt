package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import top.yukonga.miuix.kmp.basic.Surface as MiuixSurface

/**
 * Miuix owns surface drawing; caller-supplied shapes and explicit content colors are retained.
 * The legacy tonalElevation argument is accepted without adding Material's primary tint.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun Surface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = MiuixTheme.colorScheme.surface,
    contentColor: Color = miuixContentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit,
) {
    MiuixSurface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        shadowElevation = shadowElevation,
        border = border,
    ) {
        CompositionLocalProvider(MaterialLocalContentColor provides contentColor, content = content)
    }
}

/**
 * Ordinary clicks use Miuix Surface. Long-press behavior has no public Surface overload in 0.9.4,
 * so only that gesture is attached with Foundation while the framework still owns the surface.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun Surface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    color: Color = MiuixTheme.colorScheme.surface,
    contentColor: Color = miuixContentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    if (onLongClick == null) {
        MiuixSurface(
            onClick = onClick,
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .semantics { role = Role.Button },
            enabled = enabled,
            shape = shape,
            color = color,
            contentColor = contentColor,
            shadowElevation = shadowElevation,
            border = border,
            interactionSource = interactionSource,
        ) {
            CompositionLocalProvider(MaterialLocalContentColor provides contentColor, content = content)
        }
    } else {
        val source = interactionSource ?: remember { MutableInteractionSource() }
        MiuixSurface(
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .combinedClickable(
                    interactionSource = source,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
            shape = shape,
            color = color,
            contentColor = contentColor,
            shadowElevation = shadowElevation,
            border = border,
        ) {
            CompositionLocalProvider(MaterialLocalContentColor provides contentColor, content = content)
        }
    }
}

@Composable
@ReadOnlyComposable
private fun miuixContentColorFor(color: Color): Color = with(MiuixTheme.colorScheme) {
    when (color) {
        primary -> onPrimary
        primaryVariant -> onPrimaryVariant
        primaryContainer -> onPrimaryContainer
        secondary -> onSecondary
        secondaryVariant -> onSecondaryVariant
        secondaryContainer -> onSecondaryContainer
        secondaryContainerVariant -> onSecondaryContainerVariant
        tertiaryContainer -> onTertiaryContainer
        error -> onError
        errorContainer -> onErrorContainer
        background -> onBackground
        surface -> onSurface
        surfaceContainer -> onSurfaceContainer
        surfaceContainerHigh -> onSurfaceContainerHigh
        surfaceContainerHighest -> onSurfaceContainerHighest
        else -> LocalContentColor.current
    }
}
