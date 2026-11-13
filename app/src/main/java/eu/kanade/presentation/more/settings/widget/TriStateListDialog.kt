package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_cancel
import tachiyomi.i18n.action_ok
import tachiyomi.i18n.exclude
import tachiyomi.i18n.include
import tachiyomi.i18n.not_selected
import tachiyomi.presentation.core.components.miuix.MiuixPopupSystemBars
import tachiyomi.presentation.core.components.selectionSheetHeight
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixScroll
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.window.WindowDialog

private enum class State {
    CHECKED,
    INVERSED,
    UNCHECKED,
}

@Composable
fun <T> TriStateListDialog(
    title: String,
    message: String? = null,
    items: List<T>,
    initialChecked: List<T>,
    initialInversed: List<T>,
    itemLabel: @Composable (T) -> String,
    itemKey: ((T) -> Any)? = null,
    onDismissRequest: () -> Unit,
    onValueChanged: (newIncluded: List<T>, newExcluded: List<T>) -> Unit,
) {
    var show by remember { mutableStateOf(true) }
    var pendingSelection by remember { mutableStateOf<Pair<Set<Any?>, Set<Any?>>?>(null) }
    val currentItems by rememberUpdatedState(items)
    val dismiss = { show = false }
    val selected = remember { mutableStateMapOf<Any?, State>() }
    fun keyFor(item: T): Any? = itemKey?.invoke(item) ?: item
    val initialCheckedKeys = initialChecked.map(::keyFor).toSet()
    val initialInversedKeys = initialInversed.map(::keyFor).toSet()
    fun stateFor(item: T): State {
        val key = keyFor(item)
        return selected[key] ?: when (key) {
            in initialCheckedKeys -> State.CHECKED
            in initialInversedKeys -> State.INVERSED
            else -> State.UNCHECKED
        }
    }
    LaunchedEffect(items, initialCheckedKeys, initialInversedKeys) {
        items.forEach { item ->
            selected.getOrPut(keyFor(item)) { stateFor(item) }
        }
    }
    WindowDialog(
        modifier = Modifier.selectionSheetHeight(),
        show = show,
        onDismissRequest = dismiss,
        onDismissFinished = {
            // Category callers also remove this composition when committing their values.
            // Keep the native window alive until the hide animation has finished.
            pendingSelection?.let { (includedKeys, excludedKeys) ->
                onValueChanged(
                    currentItems.filter { keyFor(it) in includedKeys },
                    currentItems.filter { keyFor(it) in excludedKeys },
                )
            }
            onDismissRequest()
        },
        title = title,
        summary = message,
    ) {
        MiuixPopupSystemBars()
        Column {
            Box(modifier = Modifier.weight(1f, fill = false)) {
                val listState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier.miuixScroll(),
                    state = listState,
                    overscrollEffect = null,
                ) {
                    items(items = items, key = itemKey) { item ->
                        val state = stateFor(item)
                        val label = itemLabel(item)
                        val toggleState = when (state) {
                            State.UNCHECKED -> ToggleableState.Off
                            State.CHECKED -> ToggleableState.On
                            State.INVERSED -> ToggleableState.Indeterminate
                        }
                        val description = when (state) {
                            State.UNCHECKED -> stringResource(MR.strings.not_selected)
                            State.CHECKED -> stringResource(MR.strings.include, label)
                            State.INVERSED -> stringResource(MR.strings.exclude, label)
                        }
                        BasicComponent(
                            title = label,
                            role = Role.Checkbox,
                            enabled = show,
                            modifier = Modifier.semantics {
                                stateDescription = description
                                toggleableState = toggleState
                            },
                            startAction = { Checkbox(state = toggleState, onClick = null, enabled = show) },
                            onClick = {
                                selected[keyFor(item)] = when (stateFor(item)) {
                                    State.UNCHECKED -> State.CHECKED
                                    State.CHECKED -> State.INVERSED
                                    State.INVERSED -> State.UNCHECKED
                                }
                            },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = stringResource(MR.strings.action_cancel),
                    onClick = dismiss,
                    enabled = show,
                    modifier = Modifier.weight(1f),
                    minHeight = 48.dp,
                )
                TextButton(
                    text = stringResource(MR.strings.action_ok),
                    modifier = Modifier.weight(1f),
                    minHeight = 48.dp,
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    enabled = show,
                    onClick = {
                        val included = items.filter { stateFor(it) == State.CHECKED }.map(::keyFor).toSet()
                        val excluded = items.filter { stateFor(it) == State.INVERSED }.map(::keyFor).toSet()
                        pendingSelection = included to excluded
                        dismiss()
                    },
                )
            }
        }
    }
}
