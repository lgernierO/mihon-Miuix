package eu.kanade.presentation.track

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.AppBarIcon
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.presentation.track.components.TrackLogoIcon
import eu.kanade.tachiyomi.data.track.Tracker
import eu.kanade.tachiyomi.ui.manga.track.TrackItem
import eu.kanade.tachiyomi.util.lang.toJavaLocalDate
import eu.kanade.tachiyomi.util.system.copyToClipboard
import kotlinx.datetime.toJavaLocalDate
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.roundedfilled.VisibilityOff
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.miuix.Surface
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.components.miuix.TextButton
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixVerticalScroll
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.BadgedBox
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.menu.WindowIconDropdownMenu
import top.yukonga.miuix.kmp.preference.ArrowPreference
import java.time.format.DateTimeFormatter

@Composable
fun TrackInfoDialogHome(
    trackItems: List<TrackItem>,
    dateFormat: DateTimeFormatter,
    onStatusClick: (TrackItem) -> Unit,
    onChapterClick: (TrackItem) -> Unit,
    onScoreClick: (TrackItem) -> Unit,
    onStartDateEdit: (TrackItem) -> Unit,
    onEndDateEdit: (TrackItem) -> Unit,
    onNewSearch: (TrackItem) -> Unit,
    onOpenInBrowser: (TrackItem) -> Unit,
    onRemoved: (TrackItem) -> Unit,
    onCopyLink: (TrackItem) -> Unit,
    onTogglePrivate: (TrackItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .miuixVerticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        trackItems.forEach { item ->
            if (item.track != null) {
                val supportsScoring = item.tracker.getScoreList().isNotEmpty()
                val supportsReadingDates = item.tracker.supportsReadingDates
                val supportsPrivate = item.tracker.supportsPrivateTracking
                TrackInfoItem(
                    title = item.track.title,
                    tracker = item.tracker,
                    status = item.tracker.getStatus(item.track.status),
                    onStatusClick = { onStatusClick(item) },
                    chapters = "${item.track.lastChapterRead.toInt()}".let {
                        val totalChapters = item.track.totalChapters
                        if (totalChapters > 0) {
                            // Add known total chapter count
                            "$it / $totalChapters"
                        } else {
                            it
                        }
                    },
                    onChaptersClick = { onChapterClick(item) },
                    score = item.tracker.displayScore(item.track)
                        .takeIf { supportsScoring && item.track.score != 0.0 },
                    onScoreClick = { onScoreClick(item) }
                        .takeIf { supportsScoring },
                    startDate = remember(item.track.startDate, dateFormat) {
                        item.track.startDate?.let { dateFormat.format(it.toJavaLocalDate()) }
                    }
                        ?.takeIf { supportsReadingDates },
                    onStartDateClick = { onStartDateEdit(item) } // TODO
                        .takeIf { supportsReadingDates },
                    endDate = item.track.finishDate?.let { dateFormat.format(it.toJavaLocalDate()) }
                        ?.takeIf { supportsReadingDates },
                    onEndDateClick = { onEndDateEdit(item) }
                        .takeIf { supportsReadingDates },
                    onNewSearch = { onNewSearch(item) },
                    onOpenInBrowser = { onOpenInBrowser(item) },
                    onRemoved = { onRemoved(item) },
                    onCopyLink = { onCopyLink(item) },
                    private = item.track.private,
                    onTogglePrivate = { onTogglePrivate(item) }
                        .takeIf { supportsPrivate },
                )
            } else {
                TrackInfoItemEmpty(
                    tracker = item.tracker,
                    onNewSearch = { onNewSearch(item) },
                )
            }
        }
    }
}

@Composable
private fun TrackInfoItem(
    title: String,
    tracker: Tracker,
    status: StringResource?,
    onStatusClick: () -> Unit,
    chapters: String,
    onChaptersClick: () -> Unit,
    score: String?,
    onScoreClick: (() -> Unit)?,
    startDate: String?,
    onStartDateClick: (() -> Unit)?,
    endDate: String?,
    onEndDateClick: (() -> Unit)?,
    onNewSearch: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onRemoved: () -> Unit,
    onCopyLink: () -> Unit,
    private: Boolean,
    onTogglePrivate: (() -> Unit)?,
) {
    val context = LocalContext.current
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BadgedBox(
                badge = {
                    if (private) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.absoluteOffset(x = (-5).dp),
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.RoundedFilled.VisibilityOff,
                                contentDescription = stringResource(MR.strings.tracked_privately),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                },
            ) {
                TrackLogoIcon(
                    tracker = tracker,
                    onClick = onOpenInBrowser,
                    onLongClick = onCopyLink,
                )
            }
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .weight(1f)
                    .combinedClickable(
                        onClick = onNewSearch,
                        onLongClick = {
                            context.copyToClipboard(title, title)
                        },
                    )
                    .padding(start = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            TrackInfoItemMenu(
                onOpenInBrowser = onOpenInBrowser,
                onRemoved = onRemoved,
                onCopyLink = onCopyLink,
                private = private,
                onTogglePrivate = onTogglePrivate,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            TrackDetailsItem(
                title = stringResource(MR.strings.status),
                summary = status?.let { stringResource(it) },
                onClick = onStatusClick,
            )
            TrackDetailsItem(
                title = stringResource(MR.strings.chapters),
                summary = chapters,
                onClick = onChaptersClick,
            )
            if (onScoreClick != null) {
                TrackDetailsItem(
                    title = stringResource(MR.strings.score),
                    summary = score,
                    onClick = onScoreClick,
                )
            }
            if (onStartDateClick != null) {
                TrackDetailsItem(
                    title = stringResource(MR.strings.track_started_reading_date),
                    summary = startDate,
                    onClick = onStartDateClick,
                )
            }
            if (onEndDateClick != null) {
                TrackDetailsItem(
                    title = stringResource(MR.strings.track_finished_reading_date),
                    summary = endDate,
                    onClick = onEndDateClick,
                )
            }
        }
    }
}

@Composable
private fun TrackDetailsItem(
    title: String,
    summary: String?,
    onClick: () -> Unit,
) {
    ArrowPreference(
        title = title,
        summary = summary,
        onClick = onClick,
        modifier = Modifier.semantics { role = Role.Button },
    )
}

@Composable
private fun TrackInfoItemEmpty(
    tracker: Tracker,
    onNewSearch: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TrackLogoIcon(tracker)
        TextButton(
            onClick = onNewSearch,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
        ) {
            Text(text = stringResource(MR.strings.add_tracking))
        }
    }
}

@Composable
private fun TrackInfoItemMenu(
    onOpenInBrowser: () -> Unit,
    onRemoved: () -> Unit,
    onCopyLink: () -> Unit,
    private: Boolean,
    onTogglePrivate: (() -> Unit)?,
) {
    WindowIconDropdownMenu(
        entry = DropdownEntry(
            items = buildList {
                add(
                    DropdownItem(
                        text = stringResource(MR.strings.action_open_in_browser),
                        onClick = onOpenInBrowser,
                    ),
                )
                add(
                    DropdownItem(
                        text = stringResource(MR.strings.action_copy_link),
                        onClick = onCopyLink,
                    ),
                )
                if (onTogglePrivate != null) {
                    add(
                        DropdownItem(
                            text = stringResource(
                                if (private) {
                                    MR.strings.action_toggle_private_off
                                } else {
                                    MR.strings.action_toggle_private_on
                                },
                            ),
                            onClick = onTogglePrivate,
                        ),
                    )
                }
                add(
                    DropdownItem(
                        text = stringResource(MR.strings.action_remove),
                        onClick = onRemoved,
                    ),
                )
            },
        ),
        minWidth = 48.dp,
        minHeight = 48.dp,
    ) {
        AppBarIcon(
            imageVector = MiuixIcons.More,
            contentDescription = stringResource(MR.strings.label_more),
        )
    }
}

@PreviewLightDark
@Composable
private fun TrackInfoDialogHomePreviews(
    @PreviewParameter(TrackInfoDialogHomePreviewProvider::class)
    content: @Composable () -> Unit,
) {
    TachiyomiPreviewTheme {
        Surface {
            content()
        }
    }
}
