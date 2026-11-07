package eu.kanade.presentation.more.settings.widget

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.LocalPreferenceHighlighted
import eu.kanade.presentation.more.settings.LocalPreferenceMinHeight
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun BasePreferenceWidget(
    modifier: Modifier = Modifier,
    title: String? = null,
    subcomponent: @Composable (ColumnScope.() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    widget: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val highlighted = LocalPreferenceHighlighted.current
    val minHeight = LocalPreferenceMinHeight.current
    BasicComponent(
        modifier = modifier
            .highlightBackground(highlighted)
            .sizeIn(minHeight = minHeight)
            .fillMaxWidth(),
        startAction = icon,
        endActions = widget?.let { { it() } },
        insideMargin = if (title == null && icon == null && widget == null) {
            PaddingValues(vertical = PrefsVerticalPadding)
        } else {
            BasicComponentDefaults.InsideMargin
        },
        onClick = onClick,
        enabled = enabled,
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MiuixTheme.textStyles.headline1,
                color = if (enabled) {
                    MiuixTheme.colorScheme.onBackground
                } else {
                    MiuixTheme.colorScheme.disabledOnSecondaryVariant
                },
            )
        }
        subcomponent?.invoke(this)
    }
}

@Composable
internal fun Modifier.highlightBackground(highlighted: Boolean): Modifier {
    var highlightFlag by remember { mutableStateOf(false) }
    LaunchedEffect(highlighted) {
        highlightFlag = highlighted
        if (highlighted) {
            delay(3.seconds)
            highlightFlag = false
        }
    }
    val highlight by animateColorAsState(
        targetValue = if (highlightFlag) {
            MiuixTheme.colorScheme.primary.copy(alpha = .12f)
        } else {
            Color.Transparent
        },
        animationSpec = if (highlightFlag) {
            repeatable(
                iterations = 5,
                animation = tween(durationMillis = 200),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(
                    offsetMillis = 600,
                    offsetType = StartOffsetType.Delay,
                ),
            )
        } else {
            tween(200)
        },
        label = "highlight",
    )
    return this.background(color = highlight)
}

internal val TrailingWidgetBuffer = 16.dp
internal val PrefsHorizontalPadding = 16.dp
internal val PrefsVerticalPadding = 16.dp
