package tachiyomi.presentation.core.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.ArrowDownward
import mihon.icons.materialsymbols.rounded.ArrowUpward
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.TriState
import tachiyomi.core.common.preference.toggle
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import tachiyomi.presentation.core.util.miuixScroll
import tachiyomi.presentation.core.util.sliderValue
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.basic.Checkbox as MiuixCheckbox

object SettingsItemsPaddings {
    val Horizontal = 24.dp
    val Vertical = 10.dp
}

@Composable
fun HeadingItem(labelRes: StringResource) {
    HeadingItem(stringResource(labelRes))
}

@Composable
fun HeadingItem(text: String) {
    SmallTitle(text = text)
}

@Composable
fun SortItem(label: String, sortDescending: Boolean?, onClick: () -> Unit) {
    val arrowIcon = when (sortDescending) {
        true -> MaterialSymbols.Rounded.ArrowDownward
        false -> MaterialSymbols.Rounded.ArrowUpward
        null -> null
    }

    BaseSortItem(
        label = label,
        icon = arrowIcon,
        onClick = onClick,
    )
}

@Composable
fun BaseSortItem(label: String, icon: ImageVector?, onClick: () -> Unit) {
    BaseSettingsItem(
        label = label,
        widget = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.primary,
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        },
        onClick = onClick,
    )
}

@Composable
fun CheckboxItem(label: String, pref: Preference<Boolean>) {
    val checked by pref.collectAsState()
    CheckboxItem(
        label = label,
        checked = checked,
        onClick = { pref.toggle() },
    )
}

@Composable
fun CheckboxItem(label: String, checked: Boolean, onClick: () -> Unit) {
    CheckboxPreference(
        title = label,
        checked = checked,
        onCheckedChange = { onClick() },
        insideMargin = PaddingValues(
            horizontal = SettingsItemsPaddings.Horizontal,
            vertical = SettingsItemsPaddings.Vertical,
        ),
    )
}

@Composable
fun RadioItem(label: String, selected: Boolean, onClick: () -> Unit) {
    RadioButtonPreference(
        title = label,
        selected = selected,
        onClick = onClick,
        insideMargin = PaddingValues(
            horizontal = SettingsItemsPaddings.Horizontal,
            vertical = SettingsItemsPaddings.Vertical,
        ),
    )
}

@Composable
fun SliderItem(
    value: Int,
    valueRange: IntProgression,
    label: String,
    onChange: (Int) -> Unit,
    steps: Int = with(valueRange) { ((last - first) / step - 1).coerceAtLeast(0) },
    valueString: String = value.toString(),
    labelStyle: TextStyle = MiuixTheme.textStyles.headline1,
    pillColor: Color = MiuixTheme.colorScheme.secondaryVariant,
) {
    BaseSliderItem(
        value = value,
        valueRange = valueRange,
        steps = steps,
        title = label,
        valueString = valueString,
        onChange = onChange,
        titleStyle = labelStyle,
        pillColor = pillColor,
        modifier = Modifier.padding(
            horizontal = SettingsItemsPaddings.Horizontal,
            vertical = SettingsItemsPaddings.Vertical,
        ),
    )
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun BaseSliderItem(
    value: Int,
    valueRange: IntProgression,
    title: String,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    steps: Int = with(valueRange) { ((last - first) / step - 1).coerceAtLeast(0) },
    valueString: String = value.toString(),
    titleStyle: TextStyle = MiuixTheme.textStyles.headline1,
    subtitleStyle: TextStyle = MiuixTheme.textStyles.body2,
    pillColor: Color = MiuixTheme.colorScheme.secondaryVariant,
) {
    val minimum = minOf(valueRange.first, valueRange.last)
    val maximum = maxOf(valueRange.first, valueRange.last)
    SliderPreference(
        title = title,
        summary = subtitle,
        value = value.toFloat().coerceIn(minimum.toFloat(), maximum.toFloat()),
        valueText = valueString,
        onValueChange = { candidate ->
            // Preference flows can lag a drag frame; forwarding the return value avoids
            // dropping a fast drag back to the last displayed position.
            onChange(valueRange.sliderValue(candidate))
        },
        valueRange = minimum.toFloat()..if (minimum < maximum) maximum.toFloat() else minimum + 1f,
        steps = steps.coerceAtLeast(0),
        enabled = minimum < maximum,
        reverseDirection = valueRange.step < 0,
        hapticEffect = SliderDefaults.SliderHapticEffect.Step,
        insideMargin = PaddingValues(0.dp),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
@PreviewLightDark
fun SliderItemPreview() {
    val dark = isSystemInDarkTheme()
    val controller = remember(dark) {
        ThemeController(if (dark) ColorSchemeMode.Dark else ColorSchemeMode.Light)
    }
    MiuixTheme(controller = controller) {
        var value by remember { mutableIntStateOf(0) }
        top.yukonga.miuix.kmp.basic.Surface {
            BaseSliderItem(
                value = value,
                valueRange = 0..10,
                title = "Item per row",
                valueString = if (value == 0) "Auto" else value.toString(),
                onChange = { value = it },
                modifier = Modifier.padding(
                    horizontal = SettingsItemsPaddings.Horizontal,
                    vertical = SettingsItemsPaddings.Vertical,
                ),
            )
        }
    }
}

@Composable
fun SelectItem(
    label: String,
    options: Array<out Any?>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    WindowDropdownPreference(
        title = label,
        items = options.map { it.toString() },
        selectedIndex = selectedIndex,
        onSelectedIndexChange = onSelect,
        insideMargin = PaddingValues(
            horizontal = SettingsItemsPaddings.Horizontal,
            vertical = SettingsItemsPaddings.Vertical,
        ),
    )
}

@Composable
fun TriStateItem(
    label: String,
    state: TriState,
    enabled: Boolean = true,
    onClick: ((TriState) -> Unit)?,
) {
    val canToggle = enabled && onClick != null
    val checkboxState = when (state) {
        TriState.DISABLED -> ToggleableState.Off
        TriState.ENABLED_IS -> ToggleableState.On
        TriState.ENABLED_NOT -> ToggleableState.Indeterminate
    }
    BasicComponent(
        title = label,
        modifier = Modifier.heightIn(min = 48.dp).semantics(mergeDescendants = true) {
            role = Role.Checkbox
            toggleableState = checkboxState
            if (!canToggle) disabled()
        },
        enabled = canToggle,
        role = Role.Checkbox,
        insideMargin = PaddingValues(
            horizontal = SettingsItemsPaddings.Horizontal,
            vertical = SettingsItemsPaddings.Vertical,
        ),
        onClick = onClick?.let { callback ->
            {
                callback(
                    when (state) {
                        TriState.DISABLED -> TriState.ENABLED_IS
                        TriState.ENABLED_IS -> TriState.ENABLED_NOT
                        TriState.ENABLED_NOT -> TriState.DISABLED
                    },
                )
            }
        },
        startAction = {
            MiuixCheckbox(
                modifier = Modifier.clearAndSetSemantics {},
                state = checkboxState,
                onClick = null,
                enabled = canToggle,
            )
        },
    )
}

@Composable
fun TextItem(label: String, value: String, onChange: (String) -> Unit) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsItemsPaddings.Horizontal, vertical = 4.dp),
        label = label,
        value = value,
        onValueChange = onChange,
        singleLine = true,
    )
}

@Composable
fun SettingsChipRow(labelRes: StringResource, content: @Composable FlowRowScope.() -> Unit) {
    Column {
        HeadingItem(labelRes)
        FlowRow(
            modifier = Modifier.padding(
                start = SettingsItemsPaddings.Horizontal,
                top = 0.dp,
                end = SettingsItemsPaddings.Horizontal,
                bottom = SettingsItemsPaddings.Vertical,
            ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
            content = content,
        )
    }
}

@Composable
fun SettingsIconGrid(labelRes: StringResource, content: LazyGridScope.() -> Unit) {
    Column {
        HeadingItem(labelRes)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(128.dp),
            modifier = Modifier
                .padding(
                    start = SettingsItemsPaddings.Horizontal,
                    end = SettingsItemsPaddings.Horizontal,
                    bottom = SettingsItemsPaddings.Vertical,
                )
                .miuixScroll(),
            overscrollEffect = null,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
            content = content,
        )
    }
}

@Composable
private fun BaseSettingsItem(
    label: String,
    widget: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
) {
    BasicComponent(
        title = label,
        modifier = Modifier.heightIn(min = 48.dp),
        startAction = { Row(content = widget) },
        onClick = onClick,
        insideMargin = PaddingValues(
            horizontal = SettingsItemsPaddings.Horizontal,
            vertical = SettingsItemsPaddings.Vertical,
        ),
    )
}
