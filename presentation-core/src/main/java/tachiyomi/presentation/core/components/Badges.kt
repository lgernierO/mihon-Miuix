package tachiyomi.presentation.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Badge as MiuixBadge

@Composable
fun BadgeGroup(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.primary,
    textColor: Color = MiuixTheme.colorScheme.onPrimary,
) {
    MiuixBadge(
        modifier = modifier,
        containerColor = color,
        contentColor = textColor,
    ) {
        Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun Badge(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.primary,
    iconColor: Color = MiuixTheme.colorScheme.onPrimary,
) {
    MiuixBadge(
        modifier = modifier,
        containerColor = color,
        contentColor = iconColor,
    ) {
        Icon(
            imageVector = imageVector,
            modifier = Modifier.size(with(LocalDensity.current) { MiuixTheme.textStyles.footnote2.fontSize.toDp() }),
            contentDescription = null,
        )
    }
}
