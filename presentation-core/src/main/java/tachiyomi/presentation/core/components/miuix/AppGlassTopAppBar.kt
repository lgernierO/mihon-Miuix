package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.computedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.glass.GlassTopAppBar
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Shared with tabs in bottomContent; the native bar owns the material animation. */
internal val LocalTopBarContentScrolled = compositionLocalOf { false }

@Composable
fun AppGlassTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    color: Color = MiuixTheme.colorScheme.surface,
    expandable: Boolean = true,
    isContentScrolled: Boolean? = null,
    scrollBehavior: ScrollBehavior? = null,
    scrollState: ScrollableState? = null,
    titleAlpha: () -> Float = { 1f },
    backgroundAlpha: () -> Float = { 1f },
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
) {
    val contentScrolled by remember(scrollBehavior, scrollState, isContentScrolled) {
        computedStateOf {
            (isContentScrolled ?: scrollState?.canScrollBackward
                ?: scrollBehavior?.state?.let { it.contentOffset < -1f } ?: true) ||
                (scrollBehavior?.state?.overscrollOffset ?: 0f) < -1f
        }
    }
    val railTitleAlpha = LocalTopAppBarTitleAlpha.current
    CompositionLocalProvider(LocalTopBarContentScrolled provides contentScrolled) {
        GlassTopAppBar(
            title = title,
            isContentScrolled = contentScrolled,
            modifier = modifier,
            contentModifier = Modifier.navigationRailContentPadding(),
            expandable = expandable,
            subtitle = subtitle,
            backdrop = LocalMiuixBackdrop.current,
            progressiveBlur = true,
            bandBrush = GlassTopAppBarDefaults.bandBrush(color),
            scrollBehavior = scrollBehavior,
            titleAlpha = { titleAlpha() * railTitleAlpha() },
            backgroundAlpha = backgroundAlpha,
            centerObstructionStart = LocalTopAppBarCenterObstructionStart.current,
            navigationIcon = navigationIcon,
            actions = actions,
            bottomContent = bottomContent,
        )
    }
}
