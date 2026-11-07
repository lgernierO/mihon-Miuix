package tachiyomi.presentation.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.miuix.MiuixPopupSystemBars
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

/**
 * Miuix owns the adaptive popup window, gestures and insets. The screen's existing tablet policy
 * chooses a centered dialog or the framework bottom sheet without another outer platform Dialog.
 */
@Composable
fun AdaptiveSheet(
    isTabletUi: Boolean,
    enableImplicitDismiss: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    enableWindowDim: Boolean = true,
    content: @Composable () -> Unit,
) {
    var show by remember { mutableStateOf(true) }
    val requestDismiss: (() -> Unit)? = if (enableImplicitDismiss) {
        { show = false }
    } else {
        null
    }

    if (isTabletUi) {
        WindowDialog(
            show = show,
            modifier = Modifier.selectionSheetHeight().then(modifier),
            maxWidth = 460.dp,
            largeScreen = true,
            enableWindowDim = enableWindowDim,
            insideMargin = DpSize(0.dp, 16.dp),
            onDismissRequest = requestDismiss,
            onDismissFinished = onDismissRequest,
        ) {
            MiuixPopupSystemBars()
            content()
        }
    } else {
        WindowBottomSheet(
            show = show,
            modifier = Modifier.selectionSheetHeight().then(modifier),
            sheetMaxWidth = 460.dp,
            insideMargin = DpSize(0.dp, 0.dp),
            allowDismiss = enableImplicitDismiss,
            enableWindowDim = enableWindowDim,
            onDismissRequest = requestDismiss,
            onDismissFinished = onDismissRequest,
        ) {
            val windowInsets = remember { MutableWindowInsets() }
            MiuixPopupSystemBars(windowInsets = windowInsets)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                            .union(windowInsets)
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                    ),
                propagateMinConstraints = true,
            ) {
                content()
            }
        }
    }
}
