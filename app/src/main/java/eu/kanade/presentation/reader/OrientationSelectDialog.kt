package eu.kanade.presentation.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import dev.icerock.moko.resources.StringResource
import eu.kanade.domain.manga.model.readerOrientation
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.presentation.reader.components.ModeSelectionDialog
import eu.kanade.presentation.reader.components.ModeSelectionList
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsViewModel
import tachiyomi.i18n.MR
import tachiyomi.i18n.rotation_type
import tachiyomi.presentation.core.components.miuix.Surface
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.theme.LocalDismissState

@Composable
fun OrientationSelectDialog(
    onDismissRequest: () -> Unit,
    viewModel: ReaderSettingsViewModel,
    onChange: (StringResource) -> Unit,
) {
    val manga by viewModel.mangaFlow.collectAsState()
    val orientation = remember(manga) { ReaderOrientation.fromPreference(manga?.readerOrientation?.toInt()) }

    AdaptiveSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalDismissState.current ?: onDismissRequest
        DialogContent(
            orientation = orientation,
            onChangeOrientation = {
                viewModel.setOrientation(it)
                onChange(it.stringRes)
                dismiss()
            },
        )
    }
}

@Composable
private fun DialogContent(
    orientation: ReaderOrientation,
    onChangeOrientation: (ReaderOrientation) -> Unit,
) {
    var selected by rememberSaveable(orientation) { mutableStateOf(orientation) }

    ModeSelectionDialog(
        onUseDefault = {
            onChangeOrientation(
                ReaderOrientation.DEFAULT,
            )
        }.takeIf { orientation != ReaderOrientation.DEFAULT },
        onApply = { onChangeOrientation(selected) },
    ) {
        ModeSelectionList(
            title = stringResource(MR.strings.rotation_type),
            options = ReaderOrientation.entries,
            selectedOption = selected,
            optionTitle = { stringResource(it.stringRes) },
            optionIcon = { it.icon },
            onSelect = { selected = it },
        )
    }
}

@PreviewLightDark
@Composable
private fun DialogContentPreview() {
    TachiyomiPreviewTheme {
        Surface {
            Column {
                DialogContent(
                    orientation = ReaderOrientation.DEFAULT,
                    onChangeOrientation = {},
                )

                DialogContent(
                    orientation = ReaderOrientation.FREE,
                    onChangeOrientation = {},
                )
            }
        }
    }
}
