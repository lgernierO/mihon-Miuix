package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.glass.GlassTopAppBar
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Share the page's scroll state with Miuix's native animated toolbar material. */
@Composable
fun AppGlassTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    color: Color = MiuixTheme.colorScheme.surface,
    scrollBehavior: ScrollBehavior? = null,
    scrollState: ScrollableState? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
) {
    val isContentScrolled by remember(scrollBehavior, scrollState) {
        derivedStateOf {
            // A short list can collapse the title before the body consumes any scroll.
            val contentScrolled = scrollState?.canScrollBackward
                ?: scrollBehavior?.state?.let { it.contentOffset < 0f }
                ?: true
            contentScrolled || (scrollBehavior?.state?.heightOffset ?: 0f) < 0f
        }
    }
    GlassTopAppBar(
        title = title,
        isContentScrolled = isContentScrolled,
        modifier = modifier,
        subtitle = subtitle,
        backdrop = LocalMiuixBackdrop.current,
        progressiveBlur = true,
        bandBrush = GlassTopAppBarDefaults.bandBrush(color),
        buttonSize = IconButtonVisualSize,
        scrollBehavior = scrollBehavior,
        visualContentOffset = scrollState?.let { state ->
            {
                val barState = scrollBehavior?.state
                val contentOffset = if (state.canScrollBackward) barState?.contentOffset ?: 0f else 0f
                contentOffset + (barState?.overscrollOffset ?: 0f)
            }
        },
        navigationIcon = navigationIcon,
        actions = actions,
        bottomContent = bottomContent,
    )
}
