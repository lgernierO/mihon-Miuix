package eu.kanade.presentation.more.settings.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Info
import tachiyomi.i18n.MR
import tachiyomi.i18n.download_ahead_info
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface

@Composable
internal fun InfoWidget(text: String) {
    BasicComponent(
        summary = text,
        startAction = {
            Icon(imageVector = MaterialSymbols.Rounded.Info, contentDescription = null)
        },
    )
}

@PreviewLightDark
@Composable
private fun InfoWidgetPreview() {
    TachiyomiPreviewTheme {
        Surface {
            InfoWidget(text = stringResource(MR.strings.download_ahead_info))
        }
    }
}
