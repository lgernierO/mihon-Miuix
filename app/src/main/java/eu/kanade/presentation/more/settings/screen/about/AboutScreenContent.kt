package eu.kanade.presentation.more.settings.screen.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.tachiyomi.R
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.ContentCopy
import mihon.icons.materialsymbols.rounded.Public
import mihon.icons.simpleicons.Discord
import mihon.icons.simpleicons.Facebook
import mihon.icons.simpleicons.Github
import mihon.icons.simpleicons.Reddit
import mihon.icons.simpleicons.SimpleIcons
import mihon.icons.simpleicons.X
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun AboutScreenContent(
    versionName: String,
    isCheckingUpdates: Boolean,
    onCopyDebugInfo: () -> Unit,
    onCheckForUpdates: (() -> Unit)?,
    onOpenWhatsNew: (() -> Unit)?,
    onOpenLicenses: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    ScrollbarLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding),
        contentPadding = PaddingValues(horizontal = AboutGroupMargin, vertical = AboutGroupMargin),
        verticalArrangement = Arrangement.spacedBy(AboutGroupMargin),
    ) {
        item(key = "identity") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mihon),
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp),
                )
                Text(
                    text = stringResource(MR.strings.app_name),
                    modifier = Modifier.semantics { heading() },
                    textAlign = TextAlign.Center,
                    style = MiuixTheme.textStyles.title2,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }
        }

        item(key = "version") {
            Card(modifier = Modifier.fillMaxWidth()) {
                BasicComponent(
                    title = stringResource(MR.strings.version),
                    summary = versionName,
                    endActions = {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.ContentCopy,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                        )
                    },
                    onClick = onCopyDebugInfo,
                    onClickLabel = stringResource(MR.strings.action_copy_to_clipboard),
                    role = Role.Button,
                )
                onCheckForUpdates?.let { onClick ->
                    ArrowPreference(
                        title = stringResource(MR.strings.check_for_updates),
                        onClick = onClick,
                        enabled = !isCheckingUpdates,
                        endActions = {
                            if (isCheckingUpdates) {
                                CircularProgressIndicator(
                                    size = 24.dp,
                                    strokeWidth = 3.dp,
                                )
                            }
                        },
                    )
                }
                onOpenWhatsNew?.let { onClick ->
                    ArrowPreference(
                        title = stringResource(MR.strings.whats_new),
                        onClick = onClick,
                    )
                }
            }
        }

        item(key = "legal") {
            Card(modifier = Modifier.fillMaxWidth()) {
                ArrowPreference(
                    title = stringResource(MR.strings.licenses),
                    onClick = onOpenLicenses,
                )
                ArrowPreference(
                    title = stringResource(MR.strings.privacy_policy),
                    onClick = onOpenPrivacyPolicy,
                )
            }
        }

        item(key = "links") {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AboutLinkIcon(
                    label = stringResource(MR.strings.website),
                    icon = MaterialSymbols.Rounded.Public,
                    onClick = { onOpenLink("https://mihon.app") },
                )
                AboutLinkIcon(
                    label = "Discord",
                    icon = SimpleIcons.Discord,
                    onClick = { onOpenLink(Constants.URL_DISCORD) },
                )
                AboutLinkIcon(
                    label = "X",
                    icon = SimpleIcons.X,
                    onClick = { onOpenLink("https://x.com/mihonapp") },
                )
                AboutLinkIcon(
                    label = "Facebook",
                    icon = SimpleIcons.Facebook,
                    onClick = { onOpenLink("https://facebook.com/mihonapp") },
                )
                AboutLinkIcon(
                    label = "Reddit",
                    icon = SimpleIcons.Reddit,
                    onClick = { onOpenLink("https://www.reddit.com/r/mihonapp") },
                )
                AboutLinkIcon(
                    label = "GitHub",
                    icon = SimpleIcons.Github,
                    onClick = { onOpenLink("https://github.com/mihonapp") },
                )
            }
        }
    }
}

@Composable
private fun AboutLinkIcon(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        minWidth = 48.dp,
        minHeight = 48.dp,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MiuixTheme.colorScheme.primary,
        )
    }
}

@PreviewLightDark
@Preview(name = "Compact, large text", widthDp = 320, heightDp = 720, fontScale = 1.3f)
@Composable
private fun AboutScreenContentPreview() {
    TachiyomiPreviewTheme {
        Surface {
            AboutScreenContent(
                versionName = "Stable v0.21.3",
                isCheckingUpdates = false,
                onCopyDebugInfo = {},
                onCheckForUpdates = {},
                onOpenWhatsNew = {},
                onOpenLicenses = {},
                onOpenPrivacyPolicy = {},
                onOpenLink = {},
            )
        }
    }
}

private val AboutGroupMargin = 12.dp
