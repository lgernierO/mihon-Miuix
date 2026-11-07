package eu.kanade.presentation.more.settings.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import eu.kanade.presentation.more.settings.LocalPreferenceHighlighted
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.preference.CheckboxLocation
import top.yukonga.miuix.kmp.preference.CheckboxPreference

@Composable
fun CheckboxPreferenceWidget(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    checked: Boolean = false,
    enabled: Boolean = true,
    onCheckedChanged: (Boolean) -> Unit,
) {
    CheckboxPreference(
        modifier = modifier.highlightBackground(LocalPreferenceHighlighted.current),
        title = title,
        summary = subtitle?.takeUnless { it.isBlank() },
        startAction = icon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        },
        checked = checked,
        onCheckedChange = onCheckedChanged,
        checkboxLocation = CheckboxLocation.End,
        enabled = enabled,
    )
}
