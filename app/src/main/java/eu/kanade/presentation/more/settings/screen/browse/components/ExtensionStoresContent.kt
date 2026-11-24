package eu.kanade.presentation.more.settings.screen.browse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.widget.PreferenceIcon
import mihon.domain.extension.model.ExtensionStore
import mihon.icons.simpleicons.Discord
import mihon.icons.simpleicons.SimpleIcons
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_copy_to_clipboard
import tachiyomi.i18n.action_delete
import tachiyomi.i18n.action_open_in_browser
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.components.miuix.CompactActionIcon
import tachiyomi.presentation.core.components.miuix.CompactIconButton
import tachiyomi.presentation.core.components.miuix.ElevatedCard
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixScroll
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Copy
import top.yukonga.miuix.kmp.icon.glass.Delete
import top.yukonga.miuix.kmp.icon.glass.GlobalVpn
import top.yukonga.miuix.kmp.icon.glass.Tags

@Composable
fun ExtensionStoresContent(
    repos: List<ExtensionStore>,
    lazyListState: LazyListState,
    paddingValues: PaddingValues,
    onCopy: (ExtensionStore) -> Unit,
    onOpenWebsite: (ExtensionStore) -> Unit,
    onOpenDiscord: (ExtensionStore) -> Unit,
    onClickDelete: (ExtensionStore) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
        modifier = modifier.miuixScroll(),
        overscrollEffect = null,
    ) {
        repos.forEach {
            item {
                ExtensionStoresListItem(
                    modifier = Modifier.animateItem(),
                    store = it,
                    onOpenWebsite = { onOpenWebsite(it) },
                    onOpenDiscord = { onOpenDiscord(it) },
                    onCopy = { onCopy(it) },
                    onDelete = { onClickDelete(it) },
                )
            }
        }
    }
}

@Composable
private fun ExtensionStoresListItem(
    store: ExtensionStore,
    onOpenWebsite: () -> Unit,
    onOpenDiscord: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.padding.medium,
                    top = MaterialTheme.padding.medium,
                    end = MaterialTheme.padding.medium,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PreferenceIcon(imageVector = MiuixIcons.Glass.Tags)
            Text(
                text = store.name,
                modifier = Modifier.weight(1f).padding(start = MaterialTheme.padding.medium),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            CompactIconButton(onClick = onOpenWebsite) {
                CompactActionIcon(
                    imageVector = MiuixIcons.Glass.GlobalVpn,
                    contentDescription = stringResource(MR.strings.action_open_in_browser),
                )
            }

            if (store.contact.discord != null) {
                CompactIconButton(onClick = onOpenDiscord) {
                    CompactActionIcon(
                        imageVector = SimpleIcons.Discord,
                        contentDescription = "Discord",
                    )
                }
            }

            CompactIconButton(onClick = onCopy) {
                CompactActionIcon(
                    imageVector = MiuixIcons.Glass.Copy,
                    contentDescription = stringResource(MR.strings.action_copy_to_clipboard),
                )
            }

            CompactIconButton(onClick = onDelete) {
                CompactActionIcon(
                    imageVector = MiuixIcons.Glass.Delete,
                    contentDescription = stringResource(MR.strings.action_delete),
                )
            }
        }
    }
}
