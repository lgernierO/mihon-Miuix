package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults

@Composable
@Suppress("UNUSED_PARAMETER")
fun ElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = modifier, content = content)
}

@Composable
fun OutlinedCard(
    modifier: Modifier = Modifier,
    colors: CardColors? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = if (colors == null) {
            CardDefaults.defaultColors()
        } else {
            CardDefaults.defaultColors(color = colors.containerColor, contentColor = colors.contentColor)
        },
        content = content,
    )
}
