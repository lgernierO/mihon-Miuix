package eu.kanade.presentation.more.settings

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.UpIcon
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
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
            TopAppBar(
                title = stringResource(titleRes),
                color = MiuixTheme.colorScheme.surface,
                navigationIcon = {
                    onBackPressed?.let {
                        IconButton(onClick = it, minWidth = 48.dp, minHeight = 48.dp) {
                            UpIcon()
                        }
                    }
                },
                actions = actions,
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
