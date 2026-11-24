package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding as foundationPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.miuix.LocalMiuixBackdrop
import tachiyomi.presentation.core.components.miuix.LocalMiuixNavigationBarPadding
import tachiyomi.presentation.core.components.miuix.navigationRailContentPadding
import tachiyomi.presentation.core.util.LocalMiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.FabPosition
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBarScrollBehavior: ScrollBehavior = MiuixScrollBehavior(
        state = rememberTopAppBarState(initialHeightOffsetLimit = 0f),
    ),
    topBar: @Composable (ScrollBehavior) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MiuixTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
    content: @Composable (PaddingValues) -> Unit,
) {
    var fabHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val backdrop = rememberLayerBackdrop {
        drawRect(containerColor.copy(alpha = 1f))
        drawContent()
    }
    val supported = isRuntimeShaderSupported()
    val navigationBarPadding = LocalMiuixNavigationBarPadding.current
    val safeContentInsets = contentWindowInsets.union(WindowInsets(bottom = navigationBarPadding))

    // Chrome samples the page body, never the chrome itself. Popups use the activity scene.
    CompositionLocalProvider(LocalMiuixBackdrop provides backdrop.takeIf { supported }) {
        top.yukonga.miuix.kmp.basic.Scaffold(
            modifier = modifier,
            topBar = { topBar(topBarScrollBehavior) },
            // Reserve the top-level floating navigation slot for this page's content and actions.
            bottomBar = {
                Box(Modifier.navigationRailContentPadding().foundationPadding(bottom = navigationBarPadding)) {
                    bottomBar()
                }
            },
            snackbarHost = snackbarHost,
            floatingActionButton = {
                Box(Modifier.navigationRailContentPadding().onSizeChanged { fabHeight = it.height }) {
                    floatingActionButton()
                }
            },
            floatingActionButtonPosition = floatingActionButtonPosition,
            containerColor = containerColor,
            contentWindowInsets = safeContentInsets,
        ) { contentPadding ->
            val paddingValues = remember(contentPadding, density) {
                FabAwarePaddingValues(contentPadding) {
                    if (fabHeight == 0) 0.dp else with(density) { fabHeight.toDp() } + FabSpacing
                }
            }
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                androidx.compose.material3.LocalContentColor provides contentColor,
                // Controls inside the body must not sample that same body recording.
                LocalMiuixBackdrop provides null,
                LocalMiuixScrollBehavior provides topBarScrollBehavior,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (supported) Modifier.layerBackdrop(backdrop) else Modifier)
                        .background(containerColor)
                        .navigationRailContentPadding(),
                ) {
                    content(paddingValues)
                }
            }
        }
    }
}

private class FabAwarePaddingValues(
    private val base: PaddingValues,
    private val extraBottom: () -> Dp,
) : PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        base.calculateLeftPadding(layoutDirection)

    override fun calculateTopPadding(): Dp = base.calculateTopPadding()

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        base.calculateRightPadding(layoutDirection)

    override fun calculateBottomPadding(): Dp = base.calculateBottomPadding() + extraBottom()
}
private val FabSpacing = 16.dp
