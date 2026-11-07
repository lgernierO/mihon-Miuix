package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun IconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    imageVector: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics {
                role = Role.Checkbox
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            },
        colors = if (checked) ButtonDefaults.buttonColorsPrimary() else ButtonDefaults.buttonColors(),
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(imageVector = imageVector, contentDescription = null)
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MiuixTheme.textStyles.body2,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
