package eu.kanade.presentation.category.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Delete
import mihon.icons.materialsymbols.rounded.DragHandle
import mihon.icons.materialsymbols.rounded.Edit
import sh.calvin.reorderable.ReorderableCollectionItemScope
import tachiyomi.domain.category.model.Category
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton

@Composable
fun ReorderableCollectionItemScope.CategoryListItem(
    category: Category,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onMoveToTop: (() -> Unit)? = null,
    onMoveToBottom: (() -> Unit)? = null,
) {
    val sortLabel = stringResource(MR.strings.action_sort)
    val moveToTopLabel = stringResource(MR.strings.action_move_to_top)
    val moveToBottomLabel = stringResource(MR.strings.action_move_to_bottom)
    Card(modifier = modifier.fillMaxWidth()) {
        BasicComponent(
            title = category.name,
            onClick = onRename,
            startAction = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = sortLabel
                            customActions = buildList {
                                onMoveToTop?.let { callback ->
                                    add(
                                        CustomAccessibilityAction(moveToTopLabel) {
                                            callback()
                                            true
                                        },
                                    )
                                }
                                onMoveToBottom?.let { callback ->
                                    add(
                                        CustomAccessibilityAction(moveToBottomLabel) {
                                            callback()
                                            true
                                        },
                                    )
                                }
                            }
                        }
                        .draggableHandle(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.DragHandle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            endActions = {
                IconButton(onClick = onRename, minWidth = 48.dp, minHeight = 48.dp) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Edit,
                        contentDescription = stringResource(MR.strings.action_rename_category),
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onDelete, minWidth = 48.dp, minHeight = 48.dp) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Delete,
                        contentDescription = stringResource(MR.strings.action_delete),
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )
    }
}
