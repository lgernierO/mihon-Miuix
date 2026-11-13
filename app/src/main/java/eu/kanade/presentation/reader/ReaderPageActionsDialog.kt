package eu.kanade.presentation.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AdaptiveSheet
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.ContentCopy
import mihon.icons.materialsymbols.rounded.Photo
import mihon.icons.materialsymbols.rounded.Save
import mihon.icons.materialsymbols.rounded.Share
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_cancel
import tachiyomi.i18n.action_copy_to_clipboard
import tachiyomi.i18n.action_ok
import tachiyomi.i18n.action_save
import tachiyomi.i18n.action_share
import tachiyomi.i18n.confirm_set_image_as_cover
import tachiyomi.i18n.set_as_cover
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixScroll
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ReaderPageActionsDialog(
    onDismissRequest: () -> Unit,
    onSetAsCover: () -> Unit,
    onShare: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    var showSetCoverDialog by remember { mutableStateOf(false) }

    AdaptiveSheet(
        onDismissRequest = onDismissRequest,
    ) {
        val dismiss = LocalDismissState.current ?: onDismissRequest
        Column(Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().miuixScroll(),
                    overscrollEffect = null,
                ) {
                    if (showSetCoverDialog) {
                        item {
                            SmallTitle(text = stringResource(MR.strings.set_as_cover))
                            Text(
                                text = stringResource(MR.strings.confirm_set_image_as_cover),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary,
                            )
                        }
                    } else {
                        item {
                            PageAction(
                                title = stringResource(MR.strings.set_as_cover),
                                icon = MaterialSymbols.Rounded.Photo,
                                onClick = { showSetCoverDialog = true },
                            )
                        }
                        item {
                            PageAction(
                                title = stringResource(MR.strings.action_copy_to_clipboard),
                                icon = MaterialSymbols.Rounded.ContentCopy,
                                onClick = {
                                    onShare(true)
                                    dismiss()
                                },
                            )
                        }
                        item {
                            PageAction(
                                title = stringResource(MR.strings.action_share),
                                icon = MaterialSymbols.Rounded.Share,
                                onClick = {
                                    onShare(false)
                                    dismiss()
                                },
                            )
                        }
                        item {
                            PageAction(
                                title = stringResource(MR.strings.action_save),
                                icon = MaterialSymbols.Rounded.Save,
                                onClick = {
                                    onSave()
                                    dismiss()
                                },
                            )
                        }
                    }
                }
            }
            if (showSetCoverDialog) {
                CoverConfirmationActions(
                    onConfirm = {
                        onSetAsCover()
                        showSetCoverDialog = false
                    },
                    onCancel = { showSetCoverDialog = false },
                )
            }
        }
    }
}

@Composable
private fun PageAction(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    BasicComponent(
        title = title,
        onClick = onClick,
        role = Role.Button,
        modifier = Modifier.heightIn(min = 56.dp),
        startAction = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
            )
        },
    )
}

@Composable
private fun CoverConfirmationActions(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onCancel,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            minHeight = 48.dp,
            insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = stringResource(MR.strings.action_cancel),
                style = MiuixTheme.textStyles.body2,
                textAlign = TextAlign.Center,
            )
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            minHeight = 48.dp,
            colors = ButtonDefaults.buttonColorsPrimary(),
            insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = stringResource(MR.strings.action_ok),
                style = MiuixTheme.textStyles.body2,
                textAlign = TextAlign.Center,
            )
        }
    }
}
