package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ListItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import androidx.compose.material3.LocalTextStyle as MaterialLocalTextStyle

/**
 * Application list rows keep composable text and accessory slots. Miuix BasicComponent owns their
 * layout and margins; Material color values are accepted only at this migration boundary.
 */
@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    overlineContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    colors: ListItemColors? = null,
    contentPadding: PaddingValues = BasicComponentDefaults.InsideMargin,
    content: @Composable () -> Unit,
) {
    val containerColor = colors?.containerColor ?: MiuixTheme.colorScheme.surface
    BasicComponent(
        modifier = modifier.background(containerColor),
        insideMargin = contentPadding,
        startAction = leadingContent?.let { slot ->
            {
                ListItemSlot(
                    color = colors?.leadingContentColor ?: MiuixTheme.colorScheme.onSurface,
                    style = MiuixTheme.textStyles.main,
                    content = slot,
                )
            }
        },
        endActions = trailingContent?.let { slot ->
            {
                ListItemSlot(
                    color = colors?.trailingContentColor ?: MiuixTheme.colorScheme.onSurfaceVariantActions,
                    style = MiuixTheme.textStyles.body2,
                    content = slot,
                )
            }
        },
    ) {
        Column {
            overlineContent?.let {
                ListItemSlot(
                    color = colors?.overlineContentColor ?: MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.footnote1,
                    content = it,
                )
                Spacer(Modifier.height(2.dp))
            }
            ListItemSlot(
                color = colors?.contentColor ?: MiuixTheme.colorScheme.onSurface,
                style = MiuixTheme.textStyles.headline1,
                content = content,
            )
            supportingContent?.let {
                Spacer(Modifier.height(2.dp))
                ListItemSlot(
                    color = colors?.supportingContentColor ?: MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.body2,
                    content = it,
                )
            }
        }
    }
}

/** Covers older headline-slot call sites while using the same Miuix row. */
@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    colors: ListItemColors? = null,
) {
    ListItem(
        modifier = modifier,
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = colors,
        content = headlineContent,
    )
}

@Composable
private fun ListItemSlot(
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
