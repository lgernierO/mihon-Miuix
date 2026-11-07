package eu.kanade.presentation.reader.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.presentation.util.isTabletUi
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.SkipNext
import mihon.icons.materialsymbols.rounded.SkipPrevious
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.VerticalSlider
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

enum class ChapterNavigatorType {
    HORIZONTAL_LTR,
    HORIZONTAL_RTL,
    VERTICAL_LEFT,
    VERTICAL_RIGHT,
    ;

    fun isHorizontal() = this in setOf(HORIZONTAL_LTR, HORIZONTAL_RTL)
}

@Composable
fun ChapterNavigator(
    type: ChapterNavigatorType,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPageIndexChange: (Int) -> Unit,
    onPageIndexChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeTotalPages = totalPages.coerceAtLeast(1)
    val displayedPage = currentPage.coerceIn(1, safeTotalPages)
    var dragValue by remember(safeTotalPages) {
        mutableFloatStateOf(displayedPage.toFloat())
    }
    var dragging by remember(safeTotalPages) { mutableStateOf(false) }
    val currentOnFinished by rememberUpdatedState(onPageIndexChangeFinished)
    DisposableEffect(type, safeTotalPages) {
        onDispose {
            // A direction or chapter change can remove an active slider before pointer release.
            if (dragging) {
                dragging = false
                currentOnFinished()
            }
        }
    }
    LaunchedEffect(currentPage, safeTotalPages) {
        if (!dragging) dragValue = displayedPage.toFloat()
    }

    val onValueChange: (Float) -> Unit = { value ->
        dragging = true
        val previousPage = dragValue.roundToInt()
        dragValue = value.coerceIn(1f, safeTotalPages.toFloat())
        val nextPage = dragValue.roundToInt()
        if (nextPage != previousPage) onPageIndexChange(nextPage - 1)
    }
    val onValueChangeFinished: () -> Unit = {
        dragging = false
        onPageIndexChangeFinished()
    }
    val onAccessibleValueChange: (Float) -> Boolean = { value ->
        onValueChange(value.roundToInt().toFloat())
        onValueChangeFinished()
        true
    }

    val isTabletUi = isTabletUi()
    val mainAxisPadding = if (isTabletUi) 24.dp else 12.dp

    val backgroundColor = MiuixTheme.colorScheme.surfaceContainer

    if (type.isHorizontal()) {
        HorizontalChapterNavigator(
            isRtl = type == ChapterNavigatorType.HORIZONTAL_RTL,
            value = dragValue,
            onNextChapter = onNextChapter,
            enabledNext = enabledNext,
            onPreviousChapter = onPreviousChapter,
            enabledPrevious = enabledPrevious,
            currentPage = if (dragging) dragValue.roundToInt() else displayedPage,
            totalPages = safeTotalPages,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            onAccessibleValueChange = onAccessibleValueChange,
            mainAxisPadding = mainAxisPadding,
            modifier = modifier,
        )
    } else {
        VerticalChapterNavigator(
            value = dragValue,
            onNextChapter = onNextChapter,
            enabledNext = enabledNext,
            onPreviousChapter = onPreviousChapter,
            enabledPrevious = enabledPrevious,
            currentPage = if (dragging) dragValue.roundToInt() else displayedPage,
            totalPages = safeTotalPages,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            onAccessibleValueChange = onAccessibleValueChange,
            mainAxisPadding = mainAxisPadding,
            backgroundColor = backgroundColor,
            modifier = modifier,
        )
    }
}

@Composable
private fun HorizontalChapterNavigator(
    isRtl: Boolean,
    value: Float,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onAccessibleValueChange: (Float) -> Boolean,
    mainAxisPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    // The reading direction remains independent from the system layout direction.
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalContentColor provides MiuixTheme.colorScheme.onSurface,
    ) {
        Box(
            modifier = modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .padding(horizontal = mainAxisPadding + 4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavigatorButton(
                    enabled = if (isRtl) enabledNext else enabledPrevious,
                    onClick = if (isRtl) onNextChapter else onPreviousChapter,
                    imageVector = MaterialSymbols.Rounded.SkipPrevious,
                    contentDescription = stringResource(
                        if (isRtl) MR.strings.action_next_chapter else MR.strings.action_previous_chapter,
                    ),
                )
                if (totalPages > 1) {
                    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                        Row(
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            NavigatorPageNumber(currentPage, totalPages)
                            Slider(
                                value = value,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                                    .semantics { setProgress(action = onAccessibleValueChange) },
                                onValueChange = onValueChange,
                                valueRange = 1f..totalPages.toFloat(),
                                steps = (totalPages - 2).coerceAtLeast(0),
                                hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                                onValueChangeFinished = onValueChangeFinished,
                            )
                            Text(text = totalPages.toString(), style = MiuixTheme.textStyles.body2)
                        }
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
                NavigatorButton(
                    enabled = if (isRtl) enabledPrevious else enabledNext,
                    onClick = if (isRtl) onPreviousChapter else onNextChapter,
                    imageVector = MaterialSymbols.Rounded.SkipNext,
                    contentDescription = stringResource(
                        if (isRtl) MR.strings.action_previous_chapter else MR.strings.action_next_chapter,
                    ),
                )
            }
        }
    }
}

@Composable
private fun VerticalChapterNavigator(
    value: Float,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onAccessibleValueChange: (Float) -> Boolean,
    mainAxisPadding: Dp,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = mainAxisPadding),
        insideMargin = PaddingValues(vertical = 4.dp),
        colors = CardDefaults.defaultColors(
            color = backgroundColor,
            contentColor = MiuixTheme.colorScheme.onSurfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NavigatorButton(
                enabled = enabledPrevious,
                onClick = onPreviousChapter,
                imageVector = MaterialSymbols.Rounded.SkipPrevious,
                contentDescription = stringResource(MR.strings.action_previous_chapter),
                iconModifier = Modifier.rotate(90f),
            )
            if (totalPages > 1) {
                Column(
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NavigatorPageNumber(currentPage, totalPages)
                    VerticalSlider(
                        value = value,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                            .semantics { setProgress(action = onAccessibleValueChange) },
                        onValueChange = onValueChange,
                        valueRange = 1f..totalPages.toFloat(),
                        steps = (totalPages - 2).coerceAtLeast(0),
                        hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                        reverseDirection = true,
                        onValueChangeFinished = onValueChangeFinished,
                    )
                    Text(text = totalPages.toString(), style = MiuixTheme.textStyles.body2)
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
            NavigatorButton(
                enabled = enabledNext,
                onClick = onNextChapter,
                imageVector = MaterialSymbols.Rounded.SkipNext,
                contentDescription = stringResource(MR.strings.action_next_chapter),
                iconModifier = Modifier.rotate(90f),
            )
        }
    }
}

@Composable
private fun NavigatorButton(
    onClick: () -> Unit,
    enabled: Boolean,
    imageVector: ImageVector,
    contentDescription: String,
    iconModifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        minWidth = 48.dp,
        minHeight = 48.dp,
        modifier = Modifier.semantics {
            role = Role.Button
            if (!enabled) disabled()
        },
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = iconModifier.size(24.dp),
            tint = if (enabled) {
                LocalContentColor.current
            } else {
                MiuixTheme.colorScheme.disabledOnSurface
            },
        )
    }
}

@Composable
private fun NavigatorPageNumber(currentPage: Int, totalPages: Int) {
    Box(contentAlignment = Alignment.Center) {
        // Reserve the maximum digit width without adding a second accessible page label.
        Text(
            text = totalPages.toString(),
            modifier = Modifier.clearAndSetSemantics {},
            color = Color.Transparent,
            style = MiuixTheme.textStyles.body2,
        )
        Text(text = currentPage.toString(), style = MiuixTheme.textStyles.body2)
    }
}

@Preview
@Composable
private fun ChapterNavigatorPreview() {
    var currentPage by remember { mutableIntStateOf(1) }
    TachiyomiPreviewTheme {
        ChapterNavigator(
            type = ChapterNavigatorType.VERTICAL_RIGHT,
            onNextChapter = {},
            enabledNext = true,
            onPreviousChapter = {},
            enabledPrevious = true,
            currentPage = currentPage,
            totalPages = 10,
            onPageIndexChange = { currentPage = (it + 1) },
            onPageIndexChangeFinished = {},
        )
    }
}
