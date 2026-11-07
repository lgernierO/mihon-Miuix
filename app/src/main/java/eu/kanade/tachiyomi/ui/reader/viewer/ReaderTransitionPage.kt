package eu.kanade.tachiyomi.ui.reader.viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.reader.ChapterTransition
import eu.kanade.presentation.reader.reportPress
import eu.kanade.tachiyomi.ui.reader.model.ChapterTransition
import eu.kanade.tachiyomi.ui.reader.model.ReaderChapter
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Refresh
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ActionButton
import tachiyomi.presentation.core.components.miuix.CircularProgressIndicator
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ReaderTransitionPage(
    transition: ChapterTransition,
    downloadState: TransitionDownloadState?,
    onRetry: (ReaderChapter) -> Unit,
    onButtonPressChange: (pressed: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    statusPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (downloadState != null) {
            ChapterTransition(
                transition,
                downloadState.currChapterDownloaded,
                downloadState.goingToChapterDownloaded,
            )
        }

        val toChapter = transition.to ?: return@Column
        val state by toChapter.stateFlow.collectAsState()
        when (val state = state) {
            is ReaderChapter.State.Loading -> Column(
                modifier = Modifier.padding(statusPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(MR.strings.transition_pages_loading),
                    color = MiuixTheme.colorScheme.onBackground,
                    style = MiuixTheme.textStyles.body1,
                    textAlign = TextAlign.Center,
                )
            }
            is ReaderChapter.State.Error -> Column(
                modifier = Modifier.padding(statusPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(MR.strings.transition_pages_error, state.error.message ?: ""),
                    color = MiuixTheme.colorScheme.onBackground,
                    style = MiuixTheme.textStyles.body1,
                    textAlign = TextAlign.Center,
                )
                ActionButton(
                    onClick = { onRetry(toChapter) },
                    title = stringResource(MR.strings.action_retry),
                    icon = MaterialSymbols.Rounded.Refresh,
                    buttonModifier = Modifier.reportPress(onButtonPressChange),
                    primary = true,
                )
            }
            is ReaderChapter.State.Wait, is ReaderChapter.State.Loaded -> {}
        }
    }
}
