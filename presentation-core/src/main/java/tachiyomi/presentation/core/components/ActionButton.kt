package tachiyomi.presentation.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.miuix.CompactButton
import tachiyomi.presentation.core.components.miuix.CompactButtonDefaults
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    CompactButton(
        modifier = modifier,
        buttonModifier = buttonModifier,
        onClick = onClick,
        colors = if (primary) ButtonDefaults.buttonColorsPrimary() else ButtonDefaults.buttonColors(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(CompactButtonDefaults.IconSize),
            )
            Text(
                text = title,
                modifier = Modifier.weight(1f, fill = false),
                style = MiuixTheme.textStyles.body2,
                textAlign = TextAlign.Center,
                softWrap = true,
                overflow = TextOverflow.Visible,
            )
        }
    }
}
