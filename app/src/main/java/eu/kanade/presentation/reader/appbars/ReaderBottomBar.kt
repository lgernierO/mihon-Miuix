package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.CropOff
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Crop
import mihon.icons.materialsymbols.rounded.Settings
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_settings
import tachiyomi.i18n.pref_crop_borders
import tachiyomi.i18n.rotation_type
import tachiyomi.i18n.viewer
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ReaderBottomBar(
    readingMode: ReadingMode,
    onClickReadingMode: () -> Unit,
    orientation: ReaderOrientation,
    onClickOrientation: () -> Unit,
    cropEnabled: Boolean,
    onClickCropBorder: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier,
    chapterNavigator: (@Composable () -> Unit)? = null,
) {
    // These are reading actions, so keep their button semantics inside Miuix's bottom-bar
    // shell rather than introducing a page-selection state for NavigationBarItem.
    NavigationBar(
        modifier = modifier,
        showDivider = false,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            chapterNavigator?.invoke()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(horizontal = 8.dp)
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReaderBarAction(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = readingMode.icon,
                    title = stringResource(MR.strings.viewer),
                    onClick = onClickReadingMode,
                )
                ReaderBarAction(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = orientation.icon,
                    title = stringResource(MR.strings.rotation_type),
                    onClick = onClickOrientation,
                )
                ReaderBarAction(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = if (cropEnabled) MaterialSymbols.Rounded.Crop else CustomMaterialSymbols.Rounded.CropOff,
                    title = stringResource(MR.strings.pref_crop_borders),
                    onClick = onClickCropBorder,
                    selected = cropEnabled,
                )
                ReaderBarAction(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = MaterialSymbols.Rounded.Settings,
                    title = stringResource(MR.strings.action_settings),
                    onClick = onClickSettings,
                )
            }
        }
    }
}

@Composable
private fun ReaderBarAction(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    Box(modifier) {
        TooltipBox(text = title) {
            IconButton(
                modifier = Modifier.fillMaxWidth().fillMaxHeight().semantics { this.selected = selected },
                onClick = onClick,
            ) {
                val color = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = NavigationBarDefaults.ItemHeight)
                        .padding(vertical = NavigationBarDefaults.IconTopPadding),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(NavigationBarDefaults.IconSize),
                        tint = color,
                    )
                    Text(
                        text = title,
                        color = color,
                        style = MiuixTheme.textStyles.footnote2,
                        fontSize = NavigationBarDefaults.LabelFontSize,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
