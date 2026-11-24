package eu.kanade.presentation.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_apply
import tachiyomi.i18n.action_revert_to_default
import tachiyomi.presentation.core.components.miuix.Surface
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixScroll
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.glass.GlassDialogButton
import top.yukonga.miuix.kmp.glass.GlassDialogButtonRole
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Ok
import top.yukonga.miuix.kmp.preference.RadioButtonLocation
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ModeSelectionDialog(
    onApply: () -> Unit,
    onUseDefault: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f, fill = false)) {
            content()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            onUseDefault?.let {
                GlassDialogButton(
                    onClick = it,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    minHeight = 48.dp,
                    insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = stringResource(MR.strings.action_revert_to_default),
                        style = MiuixTheme.textStyles.body2,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            GlassDialogButton(
                onClick = onApply,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                minHeight = 48.dp,
                role = GlassDialogButtonRole.Primary,
                insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Icon(
                    imageVector = MiuixIcons.Glass.Ok,
                    modifier = Modifier.size(28.dp),
                    contentDescription = null,
                )
                Text(
                    text = stringResource(MR.strings.action_apply),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MiuixTheme.textStyles.body2,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
internal fun <T : Enum<T>> ModeSelectionList(
    title: String,
    options: List<T>,
    selectedOption: T,
    optionTitle: @Composable (T) -> String,
    optionIcon: (T) -> ImageVector,
    onSelect: (T) -> Unit,
    optionIconRotation: (T) -> Float = { 0f },
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SmallTitle(text = title)
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .selectableGroup(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().miuixScroll(),
                overscrollEffect = null,
            ) {
                items(options, key = { it.name }) { option ->
                    RadioButtonPreference(
                        title = optionTitle(option),
                        selected = option == selectedOption,
                        onClick = { onSelect(option) },
                        radioButtonLocation = RadioButtonLocation.End,
                        modifier = Modifier.semantics { selected = option == selectedOption },
                        startAction = {
                            Icon(
                                imageVector = optionIcon(option),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp).rotate(optionIconRotation(option)),
                                tint = if (option == selectedOption) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.onSurfaceVariantActions
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    TachiyomiPreviewTheme {
        Surface {
            Column {
                ModeSelectionDialog(
                    onApply = {},
                    onUseDefault = {},
                ) {
                    Text("Dummy content")
                }

                ModeSelectionDialog(
                    onApply = {},
                ) {
                    Text("Dummy content without default")
                }
            }
        }
    }
}
