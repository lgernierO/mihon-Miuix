package tachiyomi.presentation.core.components.miuix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.glass.GlassTopAppBarDefaults
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Fixed-height counterpart to the progressive GlassTopAppBar, without changing title geometry. */
@Composable
fun AppSmallTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.surface,
    titleColor: Color = MiuixTheme.colorScheme.onSurface,
    subtitle: String = "",
    subtitleColor: Color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: ScrollBehavior? = null,
    defaultWindowInsetsPadding: Boolean = true,
    titlePadding: Dp = TopAppBarDefaults.TitlePadding,
    navigationIconPadding: Dp = TopAppBarDefaults.NavigationIconPadding,
    actionIconPadding: Dp = TopAppBarDefaults.ActionIconPadding,
    bottomContent: @Composable () -> Unit = {},
) {
    ProgressiveTopBarSurface(modifier = modifier, color = color) {
        SmallTopAppBar(
            title = title,
            color = Color.Transparent,
            titleColor = titleColor,
            subtitle = subtitle,
            subtitleColor = subtitleColor,
            navigationIcon = navigationIcon,
            actions = actions,
            scrollBehavior = scrollBehavior,
            defaultWindowInsetsPadding = defaultWindowInsetsPadding,
            titlePadding = titlePadding,
            navigationIconPadding = navigationIconPadding,
            actionIconPadding = actionIconPadding,
            bottomContent = bottomContent,
        )
    }
}

/** Only the page's recorded body is sampled; the foreground is placed after the blur layer. */
@Composable
private fun ProgressiveTopBarSurface(
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val backdrop = LocalMiuixBackdrop.current
    val supported = backdrop != null && isRuntimeShaderSupported()
    val surface = color.copy(alpha = 1f)
    val overhang = GlassTopAppBarDefaults.BandOverhang
    val blurColors = BlurDefaults.blurColors(blendColors = listOf(BlendColorEntry(surface.copy(alpha = 0.3f))))
    Layout(
        modifier = modifier,
        content = {
            Box(
                Modifier
                    .graphicsLayer { alpha = color.alpha }
                    .then(
                        if (supported && backdrop != null) {
                            Modifier.progressiveTextureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                gradient = ProgressiveBlur.Top.copy(curve = 0.5f),
                                blurRadius = GlassTopAppBarDefaults.ProgressiveBlurRadius.value,
                                colors = blurColors,
                            )
                        } else {
                            // Native-view hosts and older Android versions keep a readable edge fade.
                            Modifier.drawWithCache {
                                val fadeStart = ((size.height - overhang.toPx()) / size.height.coerceAtLeast(1f))
                                    .coerceIn(0f, 1f)
                                val brush = Brush.verticalGradient(
                                    0f to surface,
                                    fadeStart to surface,
                                    1f to surface.copy(alpha = 0f),
                                )
                                onDrawBehind { drawRect(brush) }
                            }
                        },
                    ),
            )
            Box { content() }
        },
    ) { measurables, constraints ->
        val foreground = measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0))
        val backgroundHeight = if (foreground.height > 0) foreground.height + overhang.roundToPx() else 0
        val background = measurables[0].measure(Constraints.fixed(foreground.width, backgroundHeight))
        layout(foreground.width, foreground.height) {
            background.placeRelative(0, 0)
            foreground.placeRelative(0, 0)
        }
    }
}
