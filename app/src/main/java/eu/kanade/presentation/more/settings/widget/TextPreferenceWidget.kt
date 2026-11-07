package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import eu.kanade.presentation.more.settings.LocalPreferenceHighlighted
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Preview
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentColors
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TextPreferenceWidget(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MiuixTheme.colorScheme.onSurface,
    widget: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    onPreferenceClick: (() -> Unit)? = null,
) {
    val rowModifier = modifier
        .highlightBackground(LocalPreferenceHighlighted.current)
        .semantics(mergeDescendants = true) { if (!enabled) disabled() }
    val startAction: (@Composable () -> Unit)? = icon?.let {
        {
            Icon(
                imageVector = it,
                tint = if (enabled) iconTint else MiuixTheme.colorScheme.disabledOnSecondaryVariant,
                contentDescription = null,
            )
        }
    }
    if (onPreferenceClick != null && widget == null && title != null) {
        ArrowPreference(
            modifier = rowModifier,
            title = title,
            titleColor = titleColor,
            summary = subtitle?.takeUnless { it.isBlank() },
            summaryColor = summaryColor,
            startAction = startAction,
            onClick = onPreferenceClick,
            enabled = enabled,
        )
    } else {
        BasicComponent(
            modifier = rowModifier,
            title = title,
            titleColor = titleColor,
            summary = subtitle?.takeUnless { it.isBlank() },
            summaryColor = summaryColor,
            startAction = startAction,
            endActions = widget?.let { { it() } },
            onClick = onPreferenceClick,
            enabled = enabled,
        )
    }
}

@PreviewLightDark
@Composable
private fun TextPreferenceWidgetPreview() {
    TachiyomiPreviewTheme {
        Surface {
            Column {
                TextPreferenceWidget(
                    title = "Text preference with icon",
                    subtitle = "Text preference summary",
                    icon = MaterialSymbols.Rounded.Preview,
                    onPreferenceClick = {},
                )
                TextPreferenceWidget(
                    title = "Text preference",
                    subtitle = "Text preference summary",
                    onPreferenceClick = {},
                )
            }
        }
    }
}
