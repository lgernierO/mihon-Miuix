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
import top.yukonga.miuix.kmp.basic.Icon
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
    Box(
        modifier = modifier.size(AppBarActionIconSize),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .requiredSize(AppBarActionIconSize)
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
