package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.i18n.miuix_pull_to_refresh
import tachiyomi.i18n.miuix_refresh_complete
import tachiyomi.i18n.miuix_refreshing
import tachiyomi.i18n.miuix_release_to_refresh
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun PullRefresh(
    refreshing: Boolean,
    enabled: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    // Selection can disable the refresh gesture without recreating the list's remembered state.
    val currentContent by rememberUpdatedState(content)
    val movableContent = remember { movableContentOf { currentContent() } }
    if (enabled || refreshing) {
        PullToRefresh(
            isRefreshing = refreshing,
            onRefresh = { if (enabled) onRefresh() },
            modifier = modifier,
            contentPadding = indicatorPadding,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            refreshTexts = listOf(
                stringResource(MR.strings.miuix_pull_to_refresh),
                stringResource(MR.strings.miuix_release_to_refresh),
                stringResource(MR.strings.miuix_refreshing),
                stringResource(MR.strings.miuix_refresh_complete),
            ),
            refreshTextStyle = MiuixTheme.textStyles.body2.copy(
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            ),
            content = movableContent,
        )
    } else {
        // Miuix has no gesture-enabled flag; remove the refresh host while selection locks it.
        Box(modifier = modifier) { movableContent() }
    }
}
