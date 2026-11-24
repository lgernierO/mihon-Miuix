package tachiyomi.presentation.core.components.miuix

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
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
) {
    val isContentScrolled by remember(scrollBehavior) {
        derivedStateOf {
            // A short list can collapse the title before the body consumes any scroll.
            scrollBehavior?.state?.let { it.contentOffset < 0f || it.heightOffset < 0f } ?: true
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
        navigationIcon = navigationIcon,
        actions = actions,
        bottomContent = bottomContent,
    )
}
