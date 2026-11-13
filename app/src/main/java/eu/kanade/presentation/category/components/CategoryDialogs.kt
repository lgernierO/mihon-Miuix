package eu.kanade.presentation.category.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.unit.dp
import eu.kanade.core.preference.asToggleableState
import eu.kanade.presentation.category.visualName
import tachiyomi.core.common.preference.CheckboxState
import tachiyomi.domain.category.model.Category
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_add
import tachiyomi.i18n.action_add_category
import tachiyomi.i18n.action_cancel
import tachiyomi.i18n.action_edit
import tachiyomi.i18n.action_edit_categories
import tachiyomi.i18n.action_move_category
import tachiyomi.i18n.action_ok
import tachiyomi.i18n.action_rename_category
import tachiyomi.i18n.delete_category
import tachiyomi.i18n.delete_category_confirmation
import tachiyomi.i18n.error_category_exists
import tachiyomi.i18n.exclude
import tachiyomi.i18n.include
import tachiyomi.i18n.information_empty_category_dialog
import tachiyomi.i18n.information_required_plain
import tachiyomi.i18n.name
import tachiyomi.i18n.not_selected
import tachiyomi.presentation.core.components.miuix.MiuixPopupSystemBars
import tachiyomi.presentation.core.components.selectionSheetHeight
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixVerticalScroll
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.preference.CheckboxLocation
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun CategoryCreateDialog(
    onDismissRequest: () -> Unit,
    onCreate: (String) -> Unit,
    categories: List<String>,
) {
    CategoryNameDialog(
        title = stringResource(MR.strings.action_add_category),
        confirmLabel = stringResource(MR.strings.action_add),
        initialName = "",
        categories = categories,
        onDismissRequest = onDismissRequest,
        onConfirm = onCreate,
    )
}

@Composable
fun CategoryRenameDialog(
    onDismissRequest: () -> Unit,
    onRename: (String) -> Unit,
    categories: List<String>,
    category: String,
) {
    CategoryNameDialog(
        title = stringResource(MR.strings.action_rename_category),
        confirmLabel = stringResource(MR.strings.action_ok),
        initialName = category,
        categories = categories,
        onDismissRequest = onDismissRequest,
        onConfirm = onRename,
    )
}

@Composable
private fun CategoryNameDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    categories: List<String>,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var show by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf(initialName) }
    var pendingName by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val valueHasChanged = name != initialName
    val nameAlreadyExists = valueHasChanged && categories.contains(name)
    val canConfirm = valueHasChanged && name.isNotEmpty() && !nameAlreadyExists
    val duplicateMessage = stringResource(MR.strings.error_category_exists)
    val dismiss = { show = false }

    WindowDialog(
        modifier = Modifier.selectionSheetHeight(),
        title = title,
        show = show,
        onDismissRequest = dismiss,
        onDismissFinished = {
            pendingName?.let(onConfirm)
            onDismissRequest()
        },
    ) {
        MiuixPopupSystemBars()
        Column {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .miuixVerticalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .semantics { if (nameAlreadyExists) error(duplicateMessage) },
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(MR.strings.name),
                    enabled = show,
                    colors = TextFieldDefaults.textFieldColors(
                        borderColor = if (nameAlreadyExists) {
                            MiuixTheme.colorScheme.error
                        } else {
                            MiuixTheme.colorScheme.primary
                        },
                    ),
                    cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                    singleLine = true,
                )
                Text(
                    text = if (nameAlreadyExists) {
                        duplicateMessage
                    } else {
                        stringResource(MR.strings.information_required_plain)
                    },
                    color = if (nameAlreadyExists) {
                        MiuixTheme.colorScheme.error
                    } else {
                        MiuixTheme.colorScheme.onSurfaceSecondary
                    },
                    style = MiuixTheme.textStyles.footnote1,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    text = confirmLabel,
                    onClick = {
                        pendingName = name
                        show = false
                    },
                    enabled = show && canConfirm,
                    modifier = Modifier.weight(1f),
                    minHeight = 48.dp,
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
            LaunchedEffect(focusRequester) {
                focusRequester.requestFocus()
            }
        }
    }
}

@Composable
fun CategoryDeleteDialog(
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    category: String,
) {
    var show by remember { mutableStateOf(true) }
    var deleteConfirmed by remember { mutableStateOf(false) }
    val dismiss = { show = false }
    WindowDialog(
        modifier = Modifier.selectionSheetHeight(),
        title = stringResource(MR.strings.delete_category),
        summary = stringResource(MR.strings.delete_category_confirmation, category),
        show = show,
        onDismissRequest = dismiss,
        onDismissFinished = {
            if (deleteConfirmed) onDelete()
            onDismissRequest()
        },
    ) {
        MiuixPopupSystemBars()
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                onClick = {
                    deleteConfirmed = true
                    show = false
                },
                enabled = show,
                modifier = Modifier.weight(1f),
                minHeight = 48.dp,
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

@Composable
fun ChangeCategoryDialog(
    initialSelection: List<CheckboxState<Category>>,
    onDismissRequest: () -> Unit,
    onEditCategories: () -> Unit,
    onConfirm: (List<Long>, List<Long>) -> Unit,
) {
    var show by remember { mutableStateOf(true) }
    var editRequested by remember { mutableStateOf(false) }
    var pendingSelection by remember { mutableStateOf<List<CheckboxState<Category>>?>(null) }
    var selection by remember { mutableStateOf(initialSelection) }
    val dismiss = { show = false }
    WindowDialog(
        modifier = Modifier.selectionSheetHeight(),
        show = show,
        title = stringResource(MR.strings.action_move_category),
        summary = if (initialSelection.isEmpty()) {
            stringResource(MR.strings.information_empty_category_dialog)
        } else {
            null
        },
        onDismissRequest = dismiss,
        onDismissFinished = {
            val committedSelection = pendingSelection
            onDismissRequest()
            if (editRequested) {
                onEditCategories()
            } else if (committedSelection != null) {
                onConfirm(
                    committedSelection
                        .filter { it is CheckboxState.State.Checked || it is CheckboxState.TriState.Include }
                        .map { it.value.id },
                    committedSelection
                        .filter { it is CheckboxState.State.None || it is CheckboxState.TriState.None }
                        .map { it.value.id },
                )
            }
        },
    ) {
        MiuixPopupSystemBars()
        Column {
            if (selection.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .miuixVerticalScroll(rememberScrollState()),
                ) {
                    selection.forEach { checkbox ->
                        val onChange = {
                            val index = selection.indexOf(checkbox)
                            if (index != -1) {
                                selection = selection.toMutableList().apply { this[index] = checkbox.next() }
                            }
                        }
                        when (checkbox) {
                            is CheckboxState.TriState -> {
                                val toggleState = checkbox.asToggleableState()
                                val description = when (checkbox) {
                                    is CheckboxState.TriState.None -> stringResource(MR.strings.not_selected)
                                    is CheckboxState.TriState.Include -> {
                                        stringResource(MR.strings.include, checkbox.value.visualName)
                                    }
                                    is CheckboxState.TriState.Exclude -> {
                                        stringResource(MR.strings.exclude, checkbox.value.visualName)
                                    }
                                }
                                BasicComponent(
                                    title = checkbox.value.visualName,
                                    modifier = Modifier.semantics {
                                        toggleableState = toggleState
                                        stateDescription = description
                                    },
                                    role = Role.Checkbox,
                                    enabled = show,
                                    endActions = { Checkbox(state = toggleState, onClick = null, enabled = show) },
                                    onClick = onChange,
                                )
                            }
                            is CheckboxState.State -> {
                                CheckboxPreference(
                                    title = checkbox.value.visualName,
                                    checked = checkbox.isChecked,
                                    onCheckedChange = { onChange() },
                                    checkboxLocation = CheckboxLocation.End,
                                    enabled = show,
                                )
                            }
                        }
                    }
                }
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    text = stringResource(
                        if (selection.isEmpty()) MR.strings.action_edit_categories else MR.strings.action_edit,
                    ),
                    onClick = {
                        editRequested = true
                        show = false
                    },
                    enabled = show,
                    minHeight = 48.dp,
                )
                TextButton(
                    text = stringResource(MR.strings.action_cancel),
                    onClick = dismiss,
                    enabled = show,
                    minHeight = 48.dp,
                )
                if (selection.isNotEmpty()) {
                    TextButton(
                        text = stringResource(MR.strings.action_ok),
                        onClick = {
                            pendingSelection = selection.toList()
                            show = false
                        },
                        enabled = show,
                        minHeight = 48.dp,
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        }
    }
}
