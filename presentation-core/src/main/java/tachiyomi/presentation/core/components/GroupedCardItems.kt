package tachiyomi.presentation.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.LocalContentColor

/** One lazy item per preference, retaining a single visual card for the group. */
fun <T> LazyListScope.groupedCardItems(
    items: List<T>,
    key: (T) -> Any,
    content: @Composable (T) -> Unit,
) {
    itemsIndexed(items, key = { _, item -> key(item) }, contentType = { _, _ -> "preferenceRow" }) { index, item ->
        val colors = CardDefaults.defaultColors()
        val top = if (index == 0) CardDefaults.CornerRadius else 0.dp
        val bottom = if (index == items.lastIndex) CardDefaults.CornerRadius else 0.dp
        CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .drawBehind {
                        // Half-resolution backdrop recording can expose an antialiasing seam.
                        if (index != items.lastIndex && bottom == 0.dp && colors.color.alpha == 1f) {
                            drawRect(colors.color, topLeft = Offset(0f, size.height), size = Size(size.width, 2f))
                        }
                    }
                    .squircleSurface(colors.color, top, top, bottom, bottom),
            ) { content(item) }
        }
    }
}
