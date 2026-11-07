package tachiyomi.presentation.core.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Newspaper
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.miuix.Button
import tachiyomi.presentation.core.components.miuix.OutlinedButton
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.util.miuixVerticalScroll
import tachiyomi.presentation.core.util.secondaryItemAlpha
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun InfoScreen(
    icon: ImageVector,
    headingText: String,
    subtitleText: String,
    acceptText: String,
    onAcceptClick: () -> Unit,
    canAccept: Boolean = true,
    rejectText: String? = null,
    onRejectClick: (() -> Unit)? = null,
    rejectAction: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        bottomBar = {
            NavigationBar(showDivider = false) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canAccept,
                        onClick = onAcceptClick,
                    ) {
                        Text(text = acceptText)
                    }
                    if (rejectText != null && onRejectClick != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = onRejectClick,
                            ) {
                                Text(text = rejectText)
                            }
                            rejectAction?.invoke(this)
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        // Status bar scrim
        Box(
            modifier = Modifier
                .zIndex(2f)
                .secondaryItemAlpha()
                .background(MiuixTheme.colorScheme.surface)
                .fillMaxWidth()
                .height(paddingValues.calculateTopPadding()),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .miuixVerticalScroll(rememberScrollState())
                .padding(top = 48.dp)
                .padding(horizontal = 16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(48.dp),
                tint = MiuixTheme.colorScheme.primary,
            )
            Text(
                text = headingText,
                style = MiuixTheme.textStyles.title1,
            )
            Text(
                text = subtitleText,
                modifier = Modifier
                    .secondaryItemAlpha()
                    .padding(vertical = 8.dp),
                style = MiuixTheme.textStyles.subtitle,
            )

            content()
        }
    }
}

@PreviewLightDark
@Composable
private fun InfoScaffoldPreview() {
    InfoScreen(
        icon = MaterialSymbols.Rounded.Newspaper,
        headingText = "Heading",
        subtitleText = "Subtitle",
        acceptText = "Accept",
        onAcceptClick = {},
        rejectText = "Reject",
        onRejectClick = {},
    ) {
        Text("Hello world")
    }
}
