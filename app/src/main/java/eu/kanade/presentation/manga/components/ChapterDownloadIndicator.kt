package eu.kanade.presentation.manga.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.tachiyomi.data.download.model.Download
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.DownloadArrow
import mihon.icons.custommaterialsymbols.rounded.DownloadCircle
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Error
import mihon.icons.materialsymbols.roundedfilled.CheckCircle
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.miuix.CircularProgressIndicator
import tachiyomi.presentation.core.components.miuix.DropdownMenuItem
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.secondaryItemAlpha
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class ChapterDownloadAction {
    START,
    START_NOW,
    CANCEL,
    DELETE,
}

@Composable
fun ChapterDownloadIndicator(
    enabled: Boolean,
    downloadStateProvider: () -> Download.State,
    downloadProgressProvider: () -> Int,
    onClick: (ChapterDownloadAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val downloadState = downloadStateProvider()) {
        Download.State.NOT_DOWNLOADED -> NotDownloadedIndicator(
            enabled = enabled,
            modifier = modifier,
            onClick = onClick,
        )
        Download.State.QUEUE, Download.State.DOWNLOADING -> DownloadingIndicator(
            enabled = enabled,
            modifier = modifier,
            downloadState = downloadState,
            downloadProgressProvider = downloadProgressProvider,
            onClick = onClick,
        )
        Download.State.DOWNLOADED -> DownloadedIndicator(
            enabled = enabled,
            modifier = modifier,
            onClick = onClick,
        )
        Download.State.ERROR -> ErrorIndicator(
            enabled = enabled,
            modifier = modifier,
            onClick = onClick,
        )
    }
}

@Composable
private fun NotDownloadedIndicator(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: (ChapterDownloadAction) -> Unit,
) {
    IconButton(
        modifier = modifier.secondaryItemAlpha(),
        enabled = enabled,
        onClick = { onClick(ChapterDownloadAction.START) },
        onLongClick = { onClick(ChapterDownloadAction.START_NOW) },
    ) {
        Icon(
            imageVector = CustomMaterialSymbols.Rounded.DownloadCircle,
            contentDescription = stringResource(MR.strings.manga_download),
            modifier = Modifier.size(IndicatorSize),
            tint = if (enabled) {
                MiuixTheme.colorScheme.onSurfaceVariantActions
            } else {
                MiuixTheme.colorScheme.disabledOnSurface
            },
        )
    }
}

@Composable
private fun DownloadingIndicator(
    enabled: Boolean,
    downloadState: Download.State,
    downloadProgressProvider: () -> Int,
    onClick: (ChapterDownloadAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) {
        if (!enabled) isMenuExpanded = false
    }
    val downloadProgress = downloadProgressProvider()
    val description = stringResource(MR.strings.manga_download)
    val progressDescription = stringResource(MR.strings.downloading_with_progress, downloadProgress)
    IconButton(
        modifier = modifier.semantics {
            contentDescription = description
            stateDescription = progressDescription
        },
        enabled = enabled,
        onClick = { isMenuExpanded = true },
        onLongClick = { onClick(ChapterDownloadAction.CANCEL) },
    ) {
        val color = if (enabled) {
            MiuixTheme.colorScheme.onSurfaceVariantActions
        } else {
            MiuixTheme.colorScheme.disabledOnSurface
        }
        val indeterminate = downloadState == Download.State.QUEUE ||
            (downloadState == Download.State.DOWNLOADING && downloadProgress == 0)
        if (indeterminate) {
            CircularProgressIndicator(
                modifier = IndicatorModifier,
                color = color,
                strokeWidth = IndicatorStrokeWidth,
                size = IndicatorSize - IndicatorPadding * 2,
            )
        } else {
            val animatedProgress by animateFloatAsState(
                targetValue = downloadProgress / 100f,
                animationSpec = tween(200),
                label = "progress",
            )
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = IndicatorModifier,
                color = color,
                strokeWidth = IndicatorStrokeWidth,
                size = IndicatorSize - IndicatorPadding * 2,
            )
        }
        DropdownMenu(expanded = isMenuExpanded && enabled, onDismissRequest = { isMenuExpanded = false }) {
            DropdownMenuItem(
                text = stringResource(MR.strings.action_start_downloading_now),
                enabled = enabled,
                onClick = {
                    onClick(ChapterDownloadAction.START_NOW)
                    isMenuExpanded = false
                },
            )
            DropdownMenuItem(
                text = stringResource(MR.strings.action_cancel),
                enabled = enabled,
                onClick = {
                    onClick(ChapterDownloadAction.CANCEL)
                    isMenuExpanded = false
                },
            )
        }
        Icon(
            imageVector = CustomMaterialSymbols.Rounded.DownloadArrow,
            contentDescription = null,
            modifier = ArrowModifier,
            tint = color,
        )
    }
}

@Composable
private fun DownloadedIndicator(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: (ChapterDownloadAction) -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) {
        if (!enabled) isMenuExpanded = false
    }
    val description = stringResource(MR.strings.label_downloaded)
    IconButton(
        modifier = modifier.semantics { stateDescription = description },
        enabled = enabled,
        onClick = { isMenuExpanded = true },
        onLongClick = { isMenuExpanded = true },
    ) {
        Icon(
            imageVector = MaterialSymbols.RoundedFilled.CheckCircle,
            contentDescription = stringResource(MR.strings.action_delete),
            modifier = Modifier.size(IndicatorSize),
            tint = if (enabled) {
                MiuixTheme.colorScheme.onSurfaceVariantActions
            } else {
                MiuixTheme.colorScheme.disabledOnSurface
            },
        )
        DropdownMenu(expanded = isMenuExpanded && enabled, onDismissRequest = { isMenuExpanded = false }) {
            DropdownMenuItem(
                text = stringResource(MR.strings.action_delete),
                enabled = enabled,
                onClick = {
                    onClick(ChapterDownloadAction.DELETE)
                    isMenuExpanded = false
                },
            )
        }
    }
}

@Composable
private fun ErrorIndicator(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: (ChapterDownloadAction) -> Unit,
) {
    IconButton(
        modifier = modifier,
        enabled = enabled,
        onClick = { onClick(ChapterDownloadAction.START) },
        onLongClick = { onClick(ChapterDownloadAction.START) },
    ) {
        Icon(
            imageVector = MaterialSymbols.Rounded.Error,
            contentDescription = stringResource(MR.strings.chapter_error),
            modifier = Modifier.size(IndicatorSize),
            tint = if (enabled) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.disabledOnSurface,
        )
    }
}

private val IndicatorSize = 26.dp
private val IndicatorPadding = 1.dp

private val IndicatorStrokeWidth = 2.dp

private val IndicatorModifier = Modifier
    .size(IndicatorSize)
    .padding(IndicatorPadding)
private val ArrowModifier = Modifier
    .size(IndicatorSize)
