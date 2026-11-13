package eu.kanade.presentation.category.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Add
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_add
import tachiyomi.i18n.action_add_category
import tachiyomi.presentation.core.components.miuix.SmallExtendedFloatingActionButton
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.shouldExpandFAB
import top.yukonga.miuix.kmp.basic.Icon

@Composable
fun CategoryFloatingActionButton(
    lazyListState: LazyListState,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(MR.strings.action_add_category)
    SmallExtendedFloatingActionButton(
        text = { Text(text = stringResource(MR.strings.action_add)) },
        icon = {
            Icon(
                imageVector = MaterialSymbols.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        },
        onClick = onCreate,
        expanded = lazyListState.shouldExpandFAB(),
        modifier = modifier.semantics { contentDescription = label },
    )
}
