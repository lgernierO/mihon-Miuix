package eu.kanade.presentation.more.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.AppBarActionSpacing
import eu.kanade.presentation.components.UpIcon
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.miuix.AppGlassTopAppBar
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PreferenceScaffold(
    titleRes: StringResource,
    actions: @Composable RowScope.() -> Unit = {},
    onBackPressed: (() -> Unit)? = null,
    itemsProvider: @Composable () -> List<Preference>,
) {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            AppGlassTopAppBar(
                title = stringResource(titleRes),
                navigationIcon = {
                    onBackPressed?.let {
                        IconButton(onClick = it) {
                            UpIcon()
                        }
                    }
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppBarActionSpacing),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions,
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { contentPadding ->
            PreferenceScreen(
                items = itemsProvider(),
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = contentPadding,
            )
        },
    )
}
