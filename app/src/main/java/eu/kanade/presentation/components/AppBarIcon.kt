package eu.kanade.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.theme.LocalContentColor

@Composable
fun AppBarIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    autoMirror: Boolean = false,
) {
    val mirror = autoMirror && !imageVector.autoMirror && LocalLayoutDirection.current == LayoutDirection.Rtl
    val glyphSize = when {
        imageVector === MiuixIcons.GridView -> 26.75.dp
        imageVector === MiuixIcons.ListView || imageVector === MiuixIcons.Months -> 25.5.dp
        imageVector === MiuixIcons.Copy || imageVector === MiuixIcons.SelectAll -> 25.dp
        imageVector === MiuixIcons.Edit -> 25.75.dp
        imageVector === MiuixIcons.Refresh -> 24.5.dp
        else -> AppBarActionIconSize
    }
    Box(
        modifier = modifier.size(AppBarActionIconSize),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .requiredSize(glyphSize)
                .then(
                    if (mirror) {
                        Modifier.drawWithContent {
                            val contentScope = this
                            scale(scaleX = -1f, scaleY = 1f) {
                                contentScope.drawContent()
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
        )
    }
}
