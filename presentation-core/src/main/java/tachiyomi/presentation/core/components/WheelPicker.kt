package tachiyomi.presentation.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_edit
import tachiyomi.presentation.core.components.miuix.OutlinedTextField
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.clearFocusOnSoftKeyboardHide
import tachiyomi.presentation.core.util.clickableNoIndication
import tachiyomi.presentation.core.util.showSoftKeyboard
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.NumberPickerDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

@Composable
fun WheelNumberPicker(
    items: List<Number>,
    modifier: Modifier = Modifier,
    startIndex: Int = 0,
    size: DpSize = DpSize(128.dp, 128.dp),
    onSelectionChanged: (index: Int) -> Unit = {},
    backgroundContent: (@Composable (size: DpSize) -> Unit)? = {
        WheelPickerDefaults.Background(size = it)
    },
) {
    WheelPicker(
        modifier = modifier,
        startIndex = startIndex,
        items = items,
        size = size,
        onSelectionChanged = onSelectionChanged,
        manualInputType = KeyboardType.Number,
        backgroundContent = backgroundContent,
    )
}

@Composable
fun WheelTextPicker(
    items: List<String>,
    modifier: Modifier = Modifier,
    startIndex: Int = 0,
    size: DpSize = DpSize(128.dp, 128.dp),
    onSelectionChanged: (index: Int) -> Unit = {},
    backgroundContent: (@Composable (size: DpSize) -> Unit)? = {
        WheelPickerDefaults.Background(size = it)
    },
) {
    WheelPicker(
        modifier = modifier,
        startIndex = startIndex,
        items = items,
        size = size,
        onSelectionChanged = onSelectionChanged,
        backgroundContent = backgroundContent,
    )
}

@Composable
private fun <T> WheelPicker(
    items: List<T>,
    modifier: Modifier = Modifier,
    startIndex: Int = 0,
    size: DpSize = DpSize(128.dp, 128.dp),
    onSelectionChanged: (index: Int) -> Unit = {},
    manualInputType: KeyboardType? = null,
    backgroundContent: (@Composable (size: DpSize) -> Unit)? = {
        WheelPickerDefaults.Background(size = it)
    },
) {
    var internalIndex by remember {
        mutableIntStateOf(startIndex.coerceIn(0, items.lastIndex.coerceAtLeast(0)))
    }
    var showManualInput by remember { mutableStateOf(false) }
    val currentItems by rememberUpdatedState(items)
    val currentOnSelectionChanged by rememberUpdatedState(onSelectionChanged)
    val displayedIndex = internalIndex.coerceIn(0, items.lastIndex.coerceAtLeast(0))
    val canSelect = items.size > 1
    val canEdit = canSelect && manualInputType != null
    val manualInputVisible = showManualInput && canEdit
    val editLabel = stringResource(MR.strings.action_edit)

    val internalOnSelectionChanged: (Int) -> Unit = {
        if (currentItems.isNotEmpty()) {
            val index = it.coerceIn(currentItems.indices)
            internalIndex = index
            currentOnSelectionChanged(index)
        }
    }

    LaunchedEffect(currentItems.size) {
        if (currentItems.isNotEmpty()) {
            val index = internalIndex.coerceIn(currentItems.indices)
            internalIndex = index
            currentOnSelectionChanged(index)
        }
        if (currentItems.size <= 1 || manualInputType == null) showManualInput = false
    }

    Box(
        modifier = modifier
            .height(size.height)
            .width(size.width)
            .then(
                if (manualInputVisible) {
                    Modifier
                } else {
                    Modifier.clearAndSetSemantics {
                        items.getOrNull(displayedIndex)?.let { contentDescription = it.toString() }
                        if (canSelect) {
                            progressBarRangeInfo = ProgressBarRangeInfo(
                                current = displayedIndex.toFloat(),
                                range = 0f..items.lastIndex.toFloat(),
                                steps = (items.size - 2).coerceAtLeast(0),
                            )
                            setProgress { candidate ->
                                if (candidate.isFinite()) {
                                    internalOnSelectionChanged(candidate.roundToInt())
                                    true
                                } else {
                                    false
                                }
                            }
                            if (canEdit) {
                                onClick(label = editLabel) {
                                    showManualInput = true
                                    true
                                }
                            }
                        } else {
                            disabled()
                        }
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        backgroundContent?.invoke(size)

        if (manualInputVisible) {
            val value = rememberSaveable(saver = TextFieldState.Saver) {
                val currentString = items[displayedIndex].toString()
                TextFieldState(initialText = currentString, initialSelection = TextRange(currentString.length))
            }

            val processManualInput: () -> Unit = {
                if (showManualInput) {
                    currentItems
                        .indexOfFirst { it.toString() == value.text.toString() }
                        .takeIf { it >= 0 }
                        ?.let(internalOnSelectionChanged)
                    showManualInput = false
                }
            }

            OutlinedTextField(
                fieldModifier = Modifier
                    .showSoftKeyboard(true)
                    .clearFocusOnSoftKeyboardHide(processManualInput),
                onKeyboardAction = { processManualInput() },
                state = value,
                labelText = editLabel,
                lineLimits = TextFieldLineLimits.SingleLine,
                keyboardOptions = KeyboardOptions(
                    keyboardType = manualInputType!!,
                    imeAction = ImeAction.Done,
                ),
                textStyle = MiuixTheme.textStyles.title3.copy(textAlign = TextAlign.Center),
            )
        } else if (items.isNotEmpty()) {
            key(items.size) {
                NumberPicker(
                    value = displayedIndex,
                    onValueChange = internalOnSelectionChanged,
                    modifier = if (canEdit) {
                        Modifier.clickableNoIndication { showManualInput = true }
                    } else {
                        Modifier
                    },
                    enabled = canSelect,
                    range = items.indices,
                    label = { items[it].toString() },
                    visibleItemCount = ROW_COUNT,
                    itemHeight = size.height / ROW_COUNT,
                    colors = if (backgroundContent == null) {
                        NumberPickerDefaults.colors()
                    } else {
                        NumberPickerDefaults.colors(
                            selectedTextColor = MiuixTheme.colorScheme.onSecondaryVariant,
                        )
                    },
                    textStyle = MiuixTheme.textStyles.title3,
                )
            }
        }
    }
}

object WheelPickerDefaults {
    @Composable
    fun Background(size: DpSize) {
        Card(
            modifier = Modifier.size(size.width, size.height / ROW_COUNT),
            colors = CardDefaults.defaultColors(
                color = MiuixTheme.colorScheme.secondaryVariant,
                contentColor = MiuixTheme.colorScheme.onSecondaryVariant,
            ),
        ) {}
    }

    @Composable
    fun Item(text: String) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.title3,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val ROW_COUNT = 3
