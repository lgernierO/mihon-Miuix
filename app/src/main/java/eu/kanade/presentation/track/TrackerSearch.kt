package eu.kanade.presentation.track

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBarIcon
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.components.ToolbarSearchFieldDefaults
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.util.system.openInBrowser
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.roundedfilled.CheckCircle
import mihon.icons.materialsymbols.roundedfilled.VisibilityOff
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.miuix.Button
import tachiyomi.presentation.core.components.miuix.DropdownMenuItem
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.plus
import tachiyomi.presentation.core.util.runOnEnterKeyPressed
import tachiyomi.presentation.core.util.secondaryItemAlpha
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TrackerSearch(
    state: TextFieldState,
    onDispatchQuery: () -> Unit,
    queryResult: Result<List<TrackSearch>>?,
    selected: TrackSearch?,
    onSelectedChange: (TrackSearch) -> Unit,
    onConfirmSelection: (private: Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    supportsPrivateTracking: Boolean,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val dispatchQueryAndClearFocus: () -> Unit = {
        onDispatchQuery()
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            Column {
                SmallTopAppBar(
                    title = "",
                    titlePadding = 0.dp,
                    navigationIconPadding = 0.dp,
                    actions = {
                        IconButton(onClick = onDismissRequest) {
                            AppBarIcon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = stringResource(MR.strings.action_bar_up_description),
                                autoMirror = true,
                            )
                        }
                        TextField(
                            state = state,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .heightIn(min = ToolbarSearchFieldDefaults.MinHeight)
                                .focusRequester(focusRequester)
                                .runOnEnterKeyPressed(action = dispatchQueryAndClearFocus),
                            insideMargin = ToolbarSearchFieldDefaults.InsideMargin,
                            cornerRadius = ToolbarSearchFieldDefaults.CornerRadius,
                            colors = ToolbarSearchFieldDefaults.colors(),
                            textStyle = MiuixTheme.textStyles.body1.copy(
                                color = MiuixTheme.colorScheme.onSurfaceContainerHigh,
                            ),
                            cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                            label = stringResource(MR.strings.action_search_hint),
                            useLabelAsPlaceholder = true,
                            lineLimits = TextFieldLineLimits.SingleLine,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            onKeyboardAction = { dispatchQueryAndClearFocus() },
                        )
                        if (state.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    state.clearText()
                                    focusRequester.requestFocus()
                                },
                            ) {
                                AppBarIcon(
                                    imageVector = MiuixIcons.Close,
                                    contentDescription = stringResource(MR.strings.action_reset),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selected != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = slideOutVertically { it / 2 } + fadeOut(),
            ) {
                Row(
                    modifier = Modifier
                        .padding(MaterialTheme.padding.small)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                ) {
                    Button(
                        onClick = { onConfirmSelection(false) },
                        modifier = Modifier.weight(1f),
                        elevation = ButtonDefaults.elevatedButtonElevation(),
                    ) {
                        Text(text = stringResource(MR.strings.action_track))
                    }
                    if (supportsPrivateTracking) {
                        Button(
                            onClick = { onConfirmSelection(true) },
                            elevation = ButtonDefaults.elevatedButtonElevation(),
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.RoundedFilled.VisibilityOff,
                                contentDescription = stringResource(MR.strings.action_toggle_private_on),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        if (queryResult == null) {
            LoadingScreen(modifier = Modifier.padding(innerPadding))
        } else {
            val availableTracks = queryResult.getOrNull()
            if (availableTracks != null) {
                if (availableTracks.isEmpty()) {
                    EmptyScreen(
                        modifier = Modifier.padding(innerPadding),
                        stringRes = MR.strings.no_results_found,
                    )
                } else {
                    ScrollbarLazyColumn(
                        modifier = Modifier.selectableGroup(),
                        contentPadding = innerPadding + PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = availableTracks,
                            key = { it.hashCode() },
                        ) {
                            SearchResultItem(
                                trackSearch = it,
                                selected = it == selected,
                                onClick = { onSelectedChange(it) },
                            )
                        }
                    }
                }
            } else {
                EmptyScreen(
                    modifier = Modifier.padding(innerPadding),
                    message = queryResult.exceptionOrNull()?.message
                        ?: stringResource(MR.strings.unknown_error),
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    trackSearch: TrackSearch,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard: Clipboard = LocalClipboard.current
    val focusManager = LocalFocusManager.current
    val locale = LocalLocale.current
    val type = trackSearch.publishing_type.toLowerCase(locale).capitalize(locale)
    val status = trackSearch.publishing_status.toLowerCase(locale).capitalize(locale)
    val description = trackSearch.summary.trim()
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = if (selected) {
            CardDefaults.defaultColors(
                color = MiuixTheme.colorScheme.secondaryVariant,
                contentColor = MiuixTheme.colorScheme.onSecondaryVariant,
            )
        } else {
            CardDefaults.defaultColors()
        },
        insideMargin = PaddingValues(12.dp),
        showIndication = true,
        onLongPress = { dropDownMenuExpanded = true },
        onClick = {
            focusManager.clearFocus()
            onClick()
        },
    ) {
        Box(
            Modifier.fillMaxWidth().semantics {
                role = Role.RadioButton
                this.selected = selected
            },
        ) {
            if (selected) {
                Icon(
                    imageVector = MaterialSymbols.RoundedFilled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopEnd),
                    tint = MiuixTheme.colorScheme.onSecondaryVariant,
                )
            }
            SearchResultItemDropDownMenu(
                expanded = dropDownMenuExpanded,
                onCollapseMenu = { dropDownMenuExpanded = false },
                onCopyName = {
                    scope.launch {
                        val clipEntry = ClipData.newPlainText(
                            trackSearch.title,
                            trackSearch.title,
                        ).toClipEntry()
                        clipboard.setClipEntry(clipEntry)
                    }
                },
                onOpenInBrowser = {
                    val url = trackSearch.tracking_url
                    if (url.isNotBlank()) {
                        context.openInBrowser(url)
                    }
                },
            )
            Column {
                Row {
                    MangaCover.Book(
                        data = trackSearch.cover_url,
                        modifier = Modifier.height(96.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trackSearch.title,
                            modifier = Modifier.padding(end = 28.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (trackSearch.authors.isNotEmpty() || trackSearch.artists.isNotEmpty()) {
                            Text(
                                text = (trackSearch.authors + trackSearch.artists).distinct().joinToString(),
                                modifier = Modifier.secondaryItemAlpha(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (type.isNotBlank()) {
                            SearchResultItemDetails(
                                title = stringResource(MR.strings.track_type),
                                text = type,
                            )
                        }
                        if (trackSearch.start_date.isNotBlank()) {
                            SearchResultItemDetails(
                                title = stringResource(MR.strings.label_started),
                                text = trackSearch.start_date,
                            )
                        }
                        if (status.isNotBlank()) {
                            SearchResultItemDetails(
                                title = stringResource(MR.strings.track_status),
                                text = status,
                            )
                        }
                        if (trackSearch.score != -1.0) {
                            SearchResultItemDetails(
                                title = stringResource(MR.strings.score),
                                text = trackSearch.score.toString(),
                            )
                        }
                    }
                }
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        modifier = Modifier
                            .paddingFromBaseline(top = 24.dp)
                            .secondaryItemAlpha(),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultItemDropDownMenu(
    expanded: Boolean,
    onCollapseMenu: () -> Unit,
    onCopyName: () -> Unit,
    onOpenInBrowser: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onCollapseMenu,
    ) {
        DropdownMenuItem(
            text = stringResource(MR.strings.action_copy_to_clipboard),
            onClick = {
                onCopyName()
                onCollapseMenu()
            },
        )
        DropdownMenuItem(
            text = stringResource(MR.strings.action_open_in_browser),
            onClick = {
                onOpenInBrowser()
                onCollapseMenu()
            },
        )
    }
}

@Composable
private fun SearchResultItemDetails(
    title: String,
    text: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall)) {
        Text(
            text = title,
            maxLines = 1,
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .secondaryItemAlpha(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@PreviewLightDark
@Composable
private fun TrackerSearchPreviews(
    @PreviewParameter(TrackerSearchPreviewProvider::class)
    content: @Composable () -> Unit,
) {
    TachiyomiPreviewTheme { content() }
}
