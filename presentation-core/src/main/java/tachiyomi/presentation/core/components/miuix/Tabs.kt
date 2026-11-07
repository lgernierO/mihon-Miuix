package tachiyomi.presentation.core.components.miuix

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.TabRow as MiuixTabRow

/**
 * Native Miuix tabs share the containing page/sheet canvas instead of drawing a second rectangular
 * surface behind the rounded selected item. Selection keeps a matching container/foreground pair.
 */
@Composable
fun TabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minWidth: Dp = TabRowDefaults.TabRowMinWidth,
    maxWidth: Dp = TabRowDefaults.TabRowMaxWidth,
    height: Dp = 48.dp,
) {
    if (tabs.isEmpty()) return
    MiuixTabRow(
        tabs = tabs,
        selectedTabIndex = selectedTabIndex.coerceIn(tabs.indices),
        onTabSelected = onTabSelected,
        modifier = modifier,
        colors = TabRowDefaults.tabRowColors(
            backgroundColor = Color.Transparent,
            contentColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            selectedBackgroundColor = MiuixTheme.colorScheme.secondaryVariant,
            selectedContentColor = MiuixTheme.colorScheme.onSecondaryVariant,
        ),
        minWidth = minWidth,
        maxWidth = maxWidth,
        height = height,
    )
}
