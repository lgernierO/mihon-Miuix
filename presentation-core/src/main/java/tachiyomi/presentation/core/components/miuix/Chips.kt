package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ChipColors
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.theme.contentColorWithContrast
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.theme.LocalContentColor
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor

/**
 * Miuix 0.9.4 has no Chip component. Its native Button provides the same selectable action
 * without a second, application-drawn control system.
 */
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    colors: SelectableChipColors? = null,
) {
    val defaultColors = if (selected) ButtonDefaults.buttonColorsPrimary() else ButtonDefaults.buttonColors()
    val buttonColors = if (colors == null) {
        defaultColors
    } else {
        ButtonDefaults.buttonColors(
            color = if (selected) colors.selectedContainerColor else colors.containerColor,
            contentColor = if (selected) colors.selectedLabelColor else colors.labelColor,
            disabledColor = if (selected) colors.disabledSelectedContainerColor else colors.disabledContainerColor,
            disabledContentColor = colors.disabledLabelColor,
        )
    }
    CompactButton(
        onClick = onClick,
        modifier = modifier,
        buttonModifier = Modifier.semantics {
            this.selected = selected
            role = Role.Checkbox
            toggleableState = if (selected) ToggleableState.On else ToggleableState.Off
        },
        enabled = enabled,
        colors = buttonColors,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                val iconColor = when {
                    colors == null -> LocalContentColor.current
                    !enabled -> colors.disabledLeadingIconColor
                    selected -> colors.selectedLeadingIconColor
                    else -> colors.leadingIconColor
                }
                CompositionLocalProvider(
                    LocalContentColor provides iconColor,
                    MaterialLocalContentColor provides iconColor,
                    content = it,
                )
            }
            // A weighted label collapses under a horizontal scroll row's unbounded width.
            Box { label() }
            trailingIcon?.let {
                val iconColor = when {
                    colors == null -> LocalContentColor.current
                    !enabled -> colors.disabledTrailingIconColor
                    selected -> colors.selectedTrailingIconColor
                    else -> colors.trailingIconColor
                }
                CompositionLocalProvider(
                    LocalContentColor provides iconColor,
                    MaterialLocalContentColor provides iconColor,
                    content = it,
                )
            }
        }
    }
}

@Composable
fun SuggestionChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    colors: ChipColors? = null,
) {
    CompactButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = if (colors == null) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.buttonColors(
                color = colors.containerColor,
                contentColor = colors.labelColor,
                disabledColor = colors.disabledContainerColor,
                disabledContentColor = colors.disabledLabelColor,
            )
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                val iconColor = when {
                    colors == null -> LocalContentColor.current
                    enabled -> colors.leadingIconContentColor
                    else -> colors.disabledLeadingIconContentColor
                }
                CompositionLocalProvider(
                    LocalContentColor provides iconColor,
                    MaterialLocalContentColor provides iconColor,
                    content = it,
                )
            }
            Box { label() }
        }
    }
}

/**
 * Source listing and global-search filters share one compact toolbar band. Natural label widths
 * scroll horizontally at narrow widths instead of stretching or clipping one selected action.
 */
@Composable
fun CompactActionRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = TopAppBarDefaults.TitlePadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
