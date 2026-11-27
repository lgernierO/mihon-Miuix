package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.glass.GlassPopup
import top.yukonga.miuix.kmp.glass.GlassPopupDefaults
import top.yukonga.miuix.kmp.glass.GlassPopupSizing
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import androidx.compose.material3.LocalTextStyle as MaterialLocalTextStyle

/** Composable menu slots on the OS4 glass popup, hosted outside the activity scene recording. */
@Composable
@Suppress("UNUSED_PARAMETER")
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    var anchorBounds by remember { mutableStateOf(Rect.Zero) }
    val backdrop = if (LocalMiuixPopupInRoot.current) {
        LocalMiuixPopupBackdrop.current
    } else {
        LocalMiuixBackdrop.current
    }
    val density = LocalDensity.current
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
    val translatedAnchor = with(density) {
        anchorBounds.translate(offset.x.toPx() * direction, offset.y.toPx())
    }
    Box(
        Modifier.onGloballyPositioned { coordinates ->
            anchorBounds = (coordinates.parentLayoutCoordinates ?: coordinates).boundsInWindow()
        },
    )
    GlassMenuHost(show = expanded) { menuShown ->
        Box(Modifier.fillMaxSize()) {
            GlassPopup(
                show = menuShown,
                onDismissRequest = {
                    if (properties.dismissOnBackPress && properties.dismissOnClickOutside) onDismissRequest()
                },
                anchorBounds = translatedAnchor,
                backdrop = backdrop,
                modifier = modifier,
                sizing = GlassPopupSizing(maxWidth = 320.dp),
            ) {
                CompositionLocalProvider(LocalMiuixBackdrop provides null) {
                    content()
                }
            }
        }
    }
}

@Composable
fun DropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                color = if (selected) MiuixTheme.colorScheme.primary else LocalContentColor.current,
            )
        },
        onClick = onClick,
        modifier = modifier.semantics {
            role = if (selected) Role.RadioButton else Role.Button
            this.selected = selected
        },
        enabled = enabled,
        leadingIcon = leadingIcon,
    )
}

/** Rich labels remain application content; BasicComponent owns the row's enabled interaction. */
@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = GlassPopupDefaults.ItemPaddingHorizontal,
        vertical = GlassPopupDefaults.ItemPaddingVertical,
    ),
) {
    val contentColor = if (enabled) {
        MiuixTheme.colorScheme.onSurfaceContainer
    } else {
        MiuixTheme.colorScheme.disabledOnSecondaryVariant
    }
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        MaterialLocalContentColor provides contentColor,
    ) {
        BasicComponent(
            modifier = modifier.semantics(mergeDescendants = true) {
                role = Role.Button
                if (!enabled) disabled()
            },
            insideMargin = contentPadding,
            onClick = onClick,
            role = Role.Button,
            enabled = enabled,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingIcon?.invoke()
                Column(modifier = Modifier.weight(1f)) {
                    CompositionLocalProvider(
                        MaterialLocalTextStyle provides MiuixTheme.textStyles.body1,
                        content = text,
                    )
                }
                trailingIcon?.invoke()
            }
        }
    }
}
