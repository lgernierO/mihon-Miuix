package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** A shared slot for ordinary settings icons; service logos have their own image sizing. */
@Composable
internal fun PreferenceIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MiuixTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    Box(modifier = modifier.size(28.dp), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(28.dp),
            tint = if (enabled) tint else MiuixTheme.colorScheme.disabledOnSecondaryVariant,
        )
    }
}
