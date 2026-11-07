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
import eu.kanade.domain.manga.model.readingMode
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.presentation.reader.components.ModeSelectionDialog
import eu.kanade.presentation.reader.components.ModeSelectionList
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsViewModel
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.miuix.Surface
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.theme.LocalDismissState

@Composable
fun ReadingModeSelectDialog(
    onDismissRequest: () -> Unit,
    viewModel: ReaderSettingsViewModel,
    onChange: (StringResource) -> Unit,
) {
    val manga by viewModel.mangaFlow.collectAsState()
    val readingMode = remember(manga) { ReadingMode.fromPreference(manga?.readingMode?.toInt()) }

    AdaptiveSheet(onDismissRequest = onDismissRequest) {
        val dismiss = LocalDismissState.current ?: onDismissRequest
        DialogContent(
            readingMode = readingMode,
            onChangeReadingMode = {
                viewModel.setReadingMode(it)
                onChange(it.stringRes)
                dismiss()
            },
        )
    }
}

@Composable
private fun DialogContent(
    readingMode: ReadingMode,
    onChangeReadingMode: (ReadingMode) -> Unit,
) {
    var selected by rememberSaveable(readingMode) { mutableStateOf(readingMode) }

    ModeSelectionDialog(
        onUseDefault = { onChangeReadingMode(ReadingMode.DEFAULT) }.takeIf { readingMode != ReadingMode.DEFAULT },
        onApply = { onChangeReadingMode(selected) },
    ) {
        ModeSelectionList(
            title = stringResource(MR.strings.pref_category_reading_mode),
            options = ReadingMode.entries,
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
                    readingMode = ReadingMode.DEFAULT,
                    onChangeReadingMode = {},
                )

                DialogContent(
                    readingMode = ReadingMode.LEFT_TO_RIGHT,
                    onChangeReadingMode = {},
                )
            }
        }
    }
}
