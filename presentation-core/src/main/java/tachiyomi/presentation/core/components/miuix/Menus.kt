package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowListPopup
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import androidx.compose.material3.LocalTextStyle as MaterialLocalTextStyle

/**
 * The public Miuix popup owns placement bounds, window lifetime and back gestures. The adapter
 * preserves composable menu content used by extension and chapter menus.
 */
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
    val density = LocalDensity.current
    val offsetX = with(density) { offset.x.roundToPx() }
    val offsetY = with(density) { offset.y.roundToPx() }
    val positionProvider = remember(offsetX, offsetY) {
        OffsetPopupPositionProvider(offsetX, offsetY)
    }

    WindowListPopup(
        show = expanded,
        onDismissRequest = if (properties.dismissOnBackPress && properties.dismissOnClickOutside) {
            onDismissRequest
        } else {
            null
        },
        enableWindowDim = false,
        popupPositionProvider = positionProvider,
    ) {
        MiuixPopupSystemBars()
        ListPopupColumn {
            Column(
                modifier = modifier
                    .sizeIn(minWidth = ListPopupDefaults.MinWidth, maxWidth = 320.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                content()
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Plain action labels use the framework's actual popup row. The outer menu's 8dp first/last
 * spacing combines with the row's 12dp margin to match native dropdown groups.
 */
@Composable
fun DropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Box(modifier = modifier.semantics { role = if (selected) Role.RadioButton else Role.Button }) {
        DropdownImpl(
            item = DropdownItem(
                text = text,
                enabled = enabled,
                selected = selected,
                icon = leadingIcon?.let { icon -> { modifier -> Box(modifier) { icon() } } },
            ),
            optionSize = 1,
            isSelected = selected,
            index = 0,
            enabled = enabled,
            isFirst = false,
            isLast = false,
            onSelectedIndexChange = { onClick() },
        )
    }
}

/**
 * Miuix menu entries accept plain text; application menus also need composable labels and icons.
 * BasicComponent still owns the Miuix row's interaction. Keep its leading/trailing slots in a
 * standard Row: ListPopupColumn asks for infinite intrinsic width, which overflows the internal
 * BasicComponent start/end allocation in 0.9.4 before the popup can choose its bounded width.
 */
@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = DropdownDefaults.InsideHorizontalPadding,
        vertical = DropdownDefaults.MiddleVerticalPadding,
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

private class OffsetPopupPositionProvider(
    private val offsetX: Int,
    private val offsetY: Int,
) : PopupPositionProvider {
    private val delegate = ListPopupDefaults.DropdownPositionProvider

    override fun getMargins(): PaddingValues = delegate.getMargins()

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowBounds: IntRect,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
        popupMargin: IntRect,
        alignment: PopupPositionProvider.Align,
    ): IntOffset {
        val base = delegate.calculatePosition(
            anchorBounds,
            windowBounds,
            layoutDirection,
            popupContentSize,
            popupMargin,
            alignment,
        )
        val direction = if (layoutDirection == LayoutDirection.Rtl) -1 else 1
        return IntOffset(
            x = (base.x + offsetX * direction).coerceIn(
                windowBounds.left,
                (windowBounds.right - popupContentSize.width).coerceAtLeast(windowBounds.left),
            ),
            y = (base.y + offsetY).coerceIn(
                windowBounds.top,
                (windowBounds.bottom - popupContentSize.height).coerceAtLeast(windowBounds.top),
            ),
        )
    }
}
