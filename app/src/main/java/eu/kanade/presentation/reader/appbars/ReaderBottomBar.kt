package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_settings
import tachiyomi.i18n.pref_crop_borders
import tachiyomi.i18n.rotation_type
import tachiyomi.i18n.viewer
import tachiyomi.presentation.core.components.miuix.LocalMiuixBackdrop
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationBarDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.glass.GlassFloatingToolbar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.SettingsOutline
import top.yukonga.miuix.kmp.icon.glass.Trim
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
    // Preserve both reader rows; the upstream toolbar owns shape, tint, blur and shadow.
    GlassFloatingToolbar(
        modifier = modifier,
        backdrop = LocalMiuixBackdrop.current,
        outSidePadding = PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (chapterNavigator != null) {
                Box(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                    chapterNavigator()
                }
            }
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
                    iconRotation = orientation.iconRotation,
                )
                ReaderBarAction(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = MiuixIcons.Glass.Trim,
                    title = stringResource(MR.strings.pref_crop_borders),
                    onClick = onClickCropBorder,
                    selected = cropEnabled,
                )
                ReaderBarAction(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    icon = MiuixIcons.Glass.SettingsOutline,
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
    iconRotation: Float = 0f,
) {
    Box(modifier) {
        TooltipBox(text = title) {
            val colors = MiuixTheme.colorScheme
            val contentColor = if (selected) colors.onTertiaryContainer else colors.onSurfaceContainer
            // The icon and its label remain one action target; selected is a toggle state,
            // not the transient holdDownState used by Miuix's press feedback.
            IconButton(
                modifier = Modifier.fillMaxWidth().fillMaxHeight().semantics { this.selected = selected },
                onClick = onClick,
                backgroundColor = if (selected) colors.tertiaryContainer else Color.Transparent,
            ) {
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
                        modifier = Modifier.size(28.dp).rotate(iconRotation),
                        tint = contentColor,
                    )
                    Text(
                        text = title,
                        color = contentColor,
                        style = MiuixTheme.textStyles.footnote2,
                        fontSize = NavigationBarDefaults.LabelFontSize,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
