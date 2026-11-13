package mihon.feature.migration.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarIcon
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.util.formatChapterNumber
import eu.kanade.presentation.util.rememberResourceBitmapPainter
import eu.kanade.tachiyomi.R
import mihon.feature.migration.list.models.MigratingManga
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowForward
import mihon.icons.materialsymbols.rounded.CopyAll
import mihon.icons.materialsymbols.rounded.DoneAll
import tachiyomi.domain.manga.model.Manga
import tachiyomi.i18n.MR
import tachiyomi.i18n.label_more
import tachiyomi.i18n.migrationListScreenTitle
import tachiyomi.i18n.migrationListScreenTitleWithProgress
import tachiyomi.i18n.migrationListScreen_copyActionLabel
import tachiyomi.i18n.migrationListScreen_copyNowActionLabel
import tachiyomi.i18n.migrationListScreen_latestChapterLabel
import tachiyomi.i18n.migrationListScreen_migrateActionLabel
import tachiyomi.i18n.migrationListScreen_migrateNowActionLabel
import tachiyomi.i18n.migrationListScreen_noMatchFoundText
import tachiyomi.i18n.migrationListScreen_searchManuallyActionLabel
import tachiyomi.i18n.migrationListScreen_skipActionLabel
import tachiyomi.i18n.migrationListScreen_unknownLatestChapter
import tachiyomi.presentation.core.components.Badge
import tachiyomi.presentation.core.components.BadgeGroup
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.topSmallPaddingValues
import tachiyomi.presentation.core.components.miuix.CircularProgressIndicator
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.plus
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.menu.WindowIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MigrationListScreenContent(
    items: List<MigratingManga>,
    migrationComplete: Boolean,
    finishedCount: Int,
    onItemClick: (Manga) -> Unit,
    onSearchManually: (MigratingManga) -> Unit,
    onSkip: (Long) -> Unit,
    onMigrate: (Long) -> Unit,
    onCopy: (Long) -> Unit,
    openMigrationDialog: (Boolean) -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = if (items.isNotEmpty()) {
                    stringResource(MR.strings.migrationListScreenTitleWithProgress, finishedCount, items.size)
                } else {
                    stringResource(MR.strings.migrationListScreenTitle)
                },
                actions = {
                    AppBarActions(
                        listOf(
                            AppBar.Action(
                                title = stringResource(MR.strings.migrationListScreen_copyActionLabel),
                                icon = if (items.size ==
                                    1
                                ) {
                                    MiuixIcons.Copy
                                } else {
                                    MaterialSymbols.Rounded.CopyAll
                                },
                                onClick = { openMigrationDialog(true) },
                                enabled = migrationComplete,
                            ),
                            AppBar.Action(
                                title = stringResource(MR.strings.migrationListScreen_migrateActionLabel),
                                icon = if (items.size ==
                                    1
                                ) {
                                    MiuixIcons.Ok
                                } else {
                                    MaterialSymbols.Rounded.DoneAll
                                },
                                onClick = { openMigrationDialog(false) },
                                enabled = migrationComplete,
                            ),
                        ),
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        FastScrollLazyColumn(contentPadding = contentPadding + topSmallPaddingValues) {
            items(items, key = { it.manga.id }) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .padding(
                            start = 16.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 8.dp,
                        )
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MigrationListItem(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.Top)
                            .fillMaxHeight(),
                        manga = item.manga,
                        source = item.source,
                        chapterCount = item.chapterCount,
                        latestChapter = item.latestChapter,
                        onClick = { onItemClick(item.manga) },
                    )

                    Icon(
                        imageVector = MaterialSymbols.AutoMirroredRounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 4.dp).size(18.dp),
                    )

                    val result by item.searchResult.collectAsState()
                    MigrationListItemResult(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.Top)
                            .fillMaxHeight(),
                        result = result,
                        onItemClick = onItemClick,
                    )

                    MigrationListItemAction(
                        modifier = Modifier.size(48.dp),
                        result = result,
                        onSearchManually = { onSearchManually(item) },
                        onSkip = { onSkip(item.manga.id) },
                        onMigrate = { onMigrate(item.manga.id) },
                        onCopy = { onCopy(item.manga.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun MigrationListItem(
    modifier: Modifier,
    manga: Manga,
    source: String,
    chapterCount: Int,
    latestChapter: Double?,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .widthIn(max = 180.dp)
            .fillMaxWidth()
            .padding(4.dp),
        onClick = onClick,
        showIndication = true,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MangaCover.Book.ratio),
        ) {
            MangaCover.Book(
                modifier = Modifier.fillMaxWidth(),
                data = manga,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            1f to MiuixTheme.colorScheme.surface,
                        ),
                    )
                    .fillMaxHeight(0.4f)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            )
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomStart),
                text = manga.title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MiuixTheme.textStyles.footnote1,
            )
            BadgeGroup(modifier = Modifier.padding(4.dp)) {
                Badge(text = "$chapterCount")
            }
        }

        Column(
            modifier = Modifier
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = source,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MiuixTheme.textStyles.body2,
            )
            val formattedLatestChapters = remember(latestChapter) {
                latestChapter?.let(::formatChapterNumber)
            }
            Text(
                text = stringResource(
                    MR.strings.migrationListScreen_latestChapterLabel,
                    formattedLatestChapters ?: stringResource(MR.strings.migrationListScreen_unknownLatestChapter),
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MiuixTheme.textStyles.footnote1,
            )
        }
    }
}

@Composable
fun MigrationListItemResult(
    modifier: Modifier,
    result: MigratingManga.SearchResult,
    onItemClick: (Manga) -> Unit,
) {
    Box(modifier.height(IntrinsicSize.Min)) {
        when (result) {
            MigratingManga.SearchResult.Searching -> {
                Box(
                    modifier = Modifier
                        .widthIn(max = 180.dp)
                        .fillMaxSize()
                        .aspectRatio(MangaCover.Book.ratio),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            MigratingManga.SearchResult.NotFound -> {
                Column(
                    Modifier
                        .widthIn(max = 180.dp)
                        .fillMaxSize()
                        .padding(4.dp),
                ) {
                    Image(
                        painter = rememberResourceBitmapPainter(id = R.drawable.cover_error),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(MangaCover.Book.ratio)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = stringResource(MR.strings.migrationListScreen_noMatchFoundText),
                        modifier = Modifier.padding(8.dp),
                        style = MiuixTheme.textStyles.body2,
                    )
                }
            }
            is MigratingManga.SearchResult.Success -> {
                MigrationListItem(
                    modifier = Modifier.fillMaxSize(),
                    manga = result.manga,
                    source = result.source,
                    chapterCount = result.chapterCount,
                    latestChapter = result.latestChapter,
                    onClick = { onItemClick(result.manga) },
                )
            }
        }
    }
}

@Composable
private fun MigrationListItemAction(
    modifier: Modifier,
    result: MigratingManga.SearchResult,
    onSearchManually: () -> Unit,
    onSkip: () -> Unit,
    onMigrate: () -> Unit,
    onCopy: () -> Unit,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        when (result) {
            MigratingManga.SearchResult.Searching -> {
                IconButton(onClick = onSkip) {
                    AppBarIcon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = stringResource(MR.strings.migrationListScreen_skipActionLabel),
                    )
                }
            }
            MigratingManga.SearchResult.NotFound, is MigratingManga.SearchResult.Success -> {
                WindowIconDropdownMenu(
                    entry = DropdownEntry(
                        items = buildList {
                            add(
                                DropdownItem(
                                    text = stringResource(MR.strings.migrationListScreen_searchManuallyActionLabel),
                                    onClick = onSearchManually,
                                ),
                            )
                            add(
                                DropdownItem(
                                    text = stringResource(MR.strings.migrationListScreen_skipActionLabel),
                                    onClick = onSkip,
                                ),
                            )
                            if (result is MigratingManga.SearchResult.Success) {
                                add(
                                    DropdownItem(
                                        text = stringResource(MR.strings.migrationListScreen_migrateNowActionLabel),
                                        onClick = onMigrate,
                                    ),
                                )
                                add(
                                    DropdownItem(
                                        text = stringResource(MR.strings.migrationListScreen_copyNowActionLabel),
                                        onClick = onCopy,
                                    ),
                                )
                            }
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
        }
    }
}
