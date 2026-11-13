package eu.kanade.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_cancel
import tachiyomi.i18n.action_remove
import tachiyomi.i18n.downloaded_chapters
import tachiyomi.i18n.manga_from_library
import tachiyomi.presentation.core.components.miuix.MiuixPopupSystemBars
import tachiyomi.presentation.core.components.selectionSheetHeight
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixVerticalScroll
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.CheckboxLocation
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun DeleteLibraryMangaDialog(
    containsLocalManga: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: (Boolean, Boolean) -> Unit,
) {
    var show by remember { mutableStateOf(true) }
    var removeFromLibrary by remember { mutableStateOf(false) }
    var removeDownloads by remember(containsLocalManga) { mutableStateOf(false) }
    var pendingRemoval by remember { mutableStateOf<Pair<Boolean, Boolean>?>(null) }
    val dismiss = { show = false }
    WindowDialog(
        show = show,
        modifier = Modifier.selectionSheetHeight(),
        title = stringResource(MR.strings.action_remove),
        onDismissRequest = dismiss,
        onDismissFinished = {
            val removal = pendingRemoval
            onDismissRequest()
            removal?.let { onConfirm(it.first, it.second) }
        },
    ) {
        MiuixPopupSystemBars()
        Column {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .miuixVerticalScroll(rememberScrollState()),
            ) {
                CheckboxPreference(
                    title = stringResource(MR.strings.manga_from_library),
                    checked = removeFromLibrary,
                    onCheckedChange = { removeFromLibrary = it },
                    checkboxLocation = CheckboxLocation.End,
                    enabled = show,
                )
                if (!containsLocalManga) {
                    CheckboxPreference(
                        title = stringResource(MR.strings.downloaded_chapters),
                        checked = removeDownloads,
                        onCheckedChange = { removeDownloads = it },
                        checkboxLocation = CheckboxLocation.End,
                        enabled = show,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    text = stringResource(MR.strings.action_cancel),
                    onClick = dismiss,
                    modifier = Modifier.weight(1f),
                    enabled = show,
                    minHeight = 48.dp,
                )
                TextButton(
                    text = stringResource(MR.strings.action_remove),
                    onClick = {
                        pendingRemoval = removeFromLibrary to (removeDownloads && !containsLocalManga)
                        show = false
                    },
                    modifier = Modifier.weight(1f),
                    enabled = show && (removeFromLibrary || removeDownloads),
                    minHeight = 48.dp,
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}
