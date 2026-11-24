package tachiyomi.presentation.core.components.miuix

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import top.yukonga.miuix.kmp.basic.BasicComponentColors
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.DropdownColors
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.IconButtonDefaults
import top.yukonga.miuix.kmp.glass.GlassDropdownPreference
import top.yukonga.miuix.kmp.glass.GlassIconButton
import top.yukonga.miuix.kmp.glass.GlassIconDropdownMenu
import top.yukonga.miuix.kmp.glass.GlassMotion
import top.yukonga.miuix.kmp.glass.GlassPopupDefaults
import top.yukonga.miuix.kmp.glass.GlassPopupSizing
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.glass.glassPopupAnchor
import top.yukonga.miuix.kmp.glass.glassPopupAnchorContent
import top.yukonga.miuix.kmp.glass.rememberGlassPopupAnchor
import top.yukonga.miuix.kmp.material.GlassShape
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.PopupLayout

/** Independent platform dialogs host their menus in their own Scaffold. */
val LocalMiuixPopupInRoot = compositionLocalOf { true }

/**
 * Full-page menu host kept alive through the library's public exit motion. The first mounted frame
 * is closed so the glass component can animate from its anchor; the host is removed only after all
 * transform and ordinary-popup tracks have settled, releasing its input layer without a timer.
 */
@Composable
internal fun GlassMenuHost(
    show: Boolean,
    content: @Composable (Boolean) -> Unit,
) {
    val mounted = remember { mutableStateOf(false) }
    LaunchedEffect(show) {
        if (show) mounted.value = true
    }
    if (!mounted.value) return

    val renderInRootScaffold = LocalMiuixPopupInRoot.current
    PopupLayout(
        visible = mounted,
        enterTransition = EnterTransition.None,
        exitTransition = ExitTransition.None,
        enableWindowDim = false,
        enableBackHandler = false,
        renderInRootScaffold = renderInRootScaffold,
    ) {
        var ready by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            withFrameNanos { }
            ready = true
        }
        val visible = show && ready
        val transition = updateTransition(visible, label = "glassMenuHost")
        val bounds = transition.animateFloat(
            transitionSpec = { GlassMotion.transformBounds(targetState) },
            label = "glassMenuHostBounds",
        ) { if (it) 1f else 0f }
        val center = transition.animateFloat(
            transitionSpec = { GlassMotion.transformCenter(targetState) },
            label = "glassMenuHostCenter",
        ) { if (it) 1f else 0f }
        val icon = transition.animateFloat(
            transitionSpec = { GlassMotion.transformIconMaterial(targetState) },
            label = "glassMenuHostIcon",
        ) { if (it) 1f else 0f }
        val material = transition.animateFloat(
            transitionSpec = { GlassMotion.transformContentMaterial(targetState) },
            label = "glassMenuHostMaterial",
        ) { if (it) 1f else 0f }
        val popup = transition.animateFloat(
            transitionSpec = { GlassMotion.popupMorph() },
            label = "glassMenuHostPopup",
        ) { if (it) 1f else 0f }
        val fade = transition.animateFloat(
            transitionSpec = { GlassMotion.popupMorphFade(targetState) },
            label = "glassMenuHostFade",
        ) { if (it) 1f else 0f }
        val blur = transition.animateFloat(
            transitionSpec = { GlassMotion.popupMorphBlur() },
            label = "glassMenuHostBlur",
        ) { if (it) 1f else 0f }
        val secondary = transition.animateFloat(
            transitionSpec = { GlassMotion.secondaryPopup(targetState) },
            label = "glassMenuHostSecondary",
        ) { if (it) 1f else 0f }

        LaunchedEffect(show, ready, transition) {
            if (!show) {
                snapshotFlow {
                    !transition.isRunning && !transition.currentState && !transition.targetState &&
                        bounds.value == 0f && center.value == 0f && icon.value == 0f && material.value == 0f &&
                        popup.value == 0f && fade.value == 0f && blur.value == 0f && secondary.value == 0f
                }.first { it }
                mounted.value = false
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            content(visible)
        }
    }
}

@Composable
fun WindowIconDropdownMenu(
    entry: DropdownEntry,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxHeight: Dp? = null,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    collapseOnSelection: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    backgroundColor: Color = Color.Unspecified,
    cornerRadius: Dp = IconButtonDefaults.CornerRadius,
    minHeight: Dp = IconButtonDefaults.MinHeight,
    minWidth: Dp = IconButtonDefaults.MinWidth,
    size: Dp = IconButtonVisualSize,
    glass: Boolean = true,
    content: @Composable () -> Unit,
) {
    WindowIconDropdownMenu(
        entries = remember(entry) { listOf(entry) },
        modifier = modifier,
        enabled = enabled,
        maxHeight = maxHeight,
        dropdownColors = dropdownColors,
        collapseOnSelection = collapseOnSelection,
        onExpandedChange = onExpandedChange,
        backgroundColor = backgroundColor,
        cornerRadius = cornerRadius,
        minHeight = minHeight,
        minWidth = minWidth,
        size = size,
        glass = glass,
        content = content,
    )
}

/** The public glass menu retains grouped entries, selected rows and cascading children. */
@Composable
@Suppress("UNUSED_PARAMETER")
fun WindowIconDropdownMenu(
    entries: List<DropdownEntry>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxHeight: Dp? = null,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    collapseOnSelection: Boolean = entries.size <= 1,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    backgroundColor: Color = Color.Unspecified,
    cornerRadius: Dp = IconButtonDefaults.CornerRadius,
    minHeight: Dp = IconButtonDefaults.MinHeight,
    minWidth: Dp = IconButtonDefaults.MinWidth,
    size: Dp = IconButtonVisualSize,
    glass: Boolean = true,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val anchor = rememberGlassPopupAnchor()
    val currentOnExpandedChange by rememberUpdatedState(onExpandedChange)
    val currentContent by rememberUpdatedState(content)
    val hapticFeedback = LocalHapticFeedback.current
    val actualEnabled = enabled && entries.any { it.items.isNotEmpty() }
    val buttonBackdrop = LocalMiuixBackdrop.current
    val popupBackdrop = if (LocalMiuixPopupInRoot.current) {
        LocalMiuixPopupBackdrop.current
    } else {
        buttonBackdrop
    }
    val radius = cornerRadius.coerceIn(0.dp, size / 2)
    val fill = if (backgroundColor.isSpecified) backgroundColor else GlassTopAppBarDefaults.buttonFill()
    val sizing = GlassPopupSizing(maxHeight = maxHeight ?: GlassPopupDefaults.MaxHeight)
    val setExpanded: (Boolean) -> Unit = remember {
        { next ->
            if (expanded != next) {
                expanded = next
                currentOnExpandedChange?.invoke(next)
            }
        }
    }
    LaunchedEffect(actualEnabled) {
        if (!actualEnabled) setExpanded(false)
    }

    // Legacy minWidth/minHeight describe the hit area, not the glass material or popup anchor.
    Box(
        modifier = modifier.sizeIn(
            minWidth = maxOf(minWidth, IconButtonTouchTargetSize),
            minHeight = maxOf(minHeight, IconButtonTouchTargetSize),
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (actualEnabled && glass) {
            val surfaceProgress = LocalMiuixToolbarGlassProgress.current?.value ?: 1f
            GlassIconButton(
                onClick = {
                    setExpanded(!expanded)
                    if (expanded) hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                },
                backdrop = buttonBackdrop.takeIf { surfaceProgress > 0f },
                surfaceAlpha = surfaceProgress,
                material = animatedToolbarButtonMaterial(),
                size = size,
                shape = GlassShape(radius),
                fill = fill,
                modifier = Modifier
                    .glassPopupAnchor(anchor, cornerRadius = radius, floating = true)
                    .semantics { role = Role.Button },
            ) {
                Box(modifier = Modifier.glassPopupAnchorContent(anchor)) {
                    currentContent()
                }
            }
        } else if (actualEnabled) {
            // The native button owns feedback; anchor bounds stay on the compact visual surface.
            IconButton(
                onClick = {
                    setExpanded(!expanded)
                    if (expanded) hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                },
                backgroundColor = backgroundColor.takeIf { it.isSpecified } ?: compactIconButtonBackgroundColor(true),
                cornerRadius = radius,
                minHeight = size,
                minWidth = size,
                modifier = Modifier
                    .sizeIn(minWidth = IconButtonTouchTargetSize, minHeight = IconButtonTouchTargetSize)
                    .wrapContentSize(Alignment.Center)
                    .glassPopupAnchor(anchor, cornerRadius = radius, floating = false),
            ) {
                Box(modifier = Modifier.glassPopupAnchorContent(anchor)) {
                    currentContent()
                }
            }
        } else {
            // Keep disabled input and semantics intact instead of leaving an actionable control.
            IconButton(
                onClick = {},
                enabled = false,
                backgroundColor = if (glass || backgroundColor.isSpecified) {
                    backgroundColor
                } else {
                    compactIconButtonBackgroundColor(false)
                },
                cornerRadius = cornerRadius,
                minHeight = size,
                minWidth = size,
                modifier = Modifier.semantics {
                    role = Role.Button
                    disabled()
                },
                content = currentContent,
            )
        }
    }

    GlassMenuHost(show = expanded) { visible ->
        Box(modifier = Modifier.fillMaxSize()) {
            GlassIconDropdownMenu(
                show = visible,
                onDismissRequest = { setExpanded(false) },
                anchor = anchor,
                anchorContent = { currentContent() },
                entries = entries,
                backdrop = popupBackdrop,
                secondaryBackdrop = popupBackdrop,
                sizing = sizing,
                collapseOnSelection = collapseOnSelection,
            )
        }
    }
}

/** Items-based preference adapter; selection and host lifetime remain framework owned. */
@Composable
@Suppress("UNUSED_PARAMETER")
fun WindowDropdownPreference(
    items: List<String>,
    selectedIndex: Int,
    title: String,
    modifier: Modifier = Modifier,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    startAction: (@Composable () -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    insideMargin: PaddingValues = BasicComponentDefaults.InsideMargin,
    maxHeight: Dp? = null,
    enabled: Boolean = true,
    showValue: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    onSelectedIndexChange: ((Int) -> Unit)? = null,
) {
    val renderInRootScaffold = LocalMiuixPopupInRoot.current
    val backdrop = if (renderInRootScaffold) {
        LocalMiuixPopupBackdrop.current
    } else {
        LocalMiuixBackdrop.current
    }
    GlassDropdownPreference(
        items = items,
        selectedIndex = selectedIndex,
        title = title,
        backdrop = backdrop,
        modifier = modifier,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        startAction = startAction,
        bottomAction = bottomAction,
        insideMargin = insideMargin,
        sizing = GlassPopupSizing(maxHeight = maxHeight ?: GlassPopupDefaults.MaxHeight),
        enabled = enabled,
        showValue = showValue,
        renderInRootScaffold = renderInRootScaffold,
        onExpandedChange = onExpandedChange,
        onSelectedIndexChange = { onSelectedIndexChange?.invoke(it) },
    )
}
