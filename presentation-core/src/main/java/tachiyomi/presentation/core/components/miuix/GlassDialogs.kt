package tachiyomi.presentation.core.components.miuix

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.util.LocalMiuixScrollBehavior
import top.yukonga.miuix.kmp.glass.GlassBottomSheetDefaults
import top.yukonga.miuix.kmp.glass.GlassDialogDefaults
import top.yukonga.miuix.kmp.glass.glassBottomSheetSurface
import top.yukonga.miuix.kmp.glass.glassDialogSurface
import top.yukonga.miuix.kmp.layout.BottomSheetDefaults
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayDialog

/** Shared action adapters use the glass treatment only inside a glass popup. */
val LocalGlassDialog = compositionLocalOf { false }

/**
 * Application compatibility entry point for the former window dialog. The activity's root Scaffold
 * hosts the glass surface outside its recorded scene, retaining nullable dismissal and caller-owned
 * visibility, margins and completion callbacks.
 */
@Composable
fun WindowDialog(
    show: Boolean,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = DialogDefaults.titleColor(),
    summary: String? = null,
    summaryColor: Color = DialogDefaults.summaryColor(),
    backgroundColor: Color = GlassDialogDefaults.containerColor(),
    enableWindowDim: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = DialogDefaults.outsideMargin,
    insideMargin: DpSize = DialogDefaults.insideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    maxWidth: Dp = DialogDefaults.MaxWidth,
    largeScreen: Boolean? = null,
    cornerRadius: Dp? = null,
    content: @Composable () -> Unit,
) {
    val backdrop = LocalMiuixPopupBackdrop.current
    OverlayDialog(
        show = show,
        modifier = modifier,
        title = title,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        backgroundColor = backgroundColor,
        enableWindowDim = enableWindowDim,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        outsideMargin = outsideMargin,
        insideMargin = insideMargin,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        renderInRootScaffold = true,
        maxWidth = maxWidth,
        largeScreen = largeScreen,
        cornerRadius = cornerRadius,
        dimColor = GlassDialogDefaults.ScrimColor,
        surface = glassDialogSurface(backdrop = backdrop, containerColor = backgroundColor),
    ) {
        // A locked progress dialog must consume Back instead of exposing the page below it.
        BackHandler(enabled = show && onDismissRequest == null) {}
        CompositionLocalProvider(
            LocalMiuixBackdrop provides null,
            LocalMiuixScrollBehavior provides null,
            LocalMiuixNavigationBarPadding provides 0.dp,
            LocalGlassDialog provides true,
            content = content,
        )
    }
}

/** The root-hosted glass sheet keeps the existing WindowBottomSheet call contract. */
@Composable
fun WindowBottomSheet(
    show: Boolean,
    modifier: Modifier = Modifier,
    title: String? = null,
    startAction: (@Composable () -> Unit)? = null,
    endAction: (@Composable () -> Unit)? = null,
    backgroundColor: Color = GlassBottomSheetDefaults.containerColor(),
    enableWindowDim: Boolean = true,
    cornerRadius: Dp = BottomSheetDefaults.cornerRadius,
    sheetMaxWidth: Dp = BottomSheetDefaults.maxWidth,
    onDismissRequest: (() -> Unit)? = null,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = BottomSheetDefaults.outsideMargin,
    insideMargin: DpSize = BottomSheetDefaults.insideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    dragHandleColor: Color = BottomSheetDefaults.dragHandleColor(),
    allowDismiss: Boolean = true,
    enableNestedScroll: Boolean = true,
    content: @Composable () -> Unit,
) {
    val backdrop = LocalMiuixPopupBackdrop.current
    CompositionLocalProvider(
        LocalMiuixBackdrop provides null,
        LocalMiuixScrollBehavior provides null,
        LocalMiuixNavigationBarPadding provides 0.dp,
        LocalGlassDialog provides true,
    ) {
        OverlayBottomSheet(
            show = show,
            modifier = modifier,
            title = title,
            startAction = startAction,
            endAction = endAction,
            backgroundColor = backgroundColor,
            enableWindowDim = enableWindowDim,
            cornerRadius = cornerRadius,
            sheetMaxWidth = sheetMaxWidth,
            onDismissRequest = onDismissRequest,
            onDismissFinished = onDismissFinished,
            outsideMargin = outsideMargin,
            insideMargin = insideMargin,
            defaultWindowInsetsPadding = defaultWindowInsetsPadding,
            dragHandleColor = dragHandleColor,
            allowDismiss = allowDismiss && onDismissRequest != null,
            enableNestedScroll = enableNestedScroll,
            renderInRootScaffold = true,
            dimColor = GlassBottomSheetDefaults.ScrimColor,
            surface = glassBottomSheetSurface(backdrop = backdrop, containerColor = backgroundColor),
            content = {
                BackHandler(enabled = show && (!allowDismiss || onDismissRequest == null)) {}
                CompositionLocalProvider(
                    LocalMiuixBackdrop provides null,
                    LocalMiuixScrollBehavior provides null,
                    LocalMiuixNavigationBarPadding provides 0.dp,
                    LocalGlassDialog provides true,
                    content = content,
                )
            },
        )
    }
}
