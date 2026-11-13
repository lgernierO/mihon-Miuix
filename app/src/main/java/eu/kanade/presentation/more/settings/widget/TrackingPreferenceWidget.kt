package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.LocalPreferenceHighlighted
import eu.kanade.presentation.track.components.TrackLogoIcon
import eu.kanade.tachiyomi.data.track.EnhancedTracker
import eu.kanade.tachiyomi.data.track.Tracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Check
import mihon.icons.materialsymbols.rounded.Refresh
import tachiyomi.i18n.MR
import tachiyomi.i18n.login_success
import tachiyomi.i18n.refresh_tracker_profile
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TrackingPreferenceWidget(
    modifier: Modifier = Modifier,
    tracker: Tracker,
    isLoggedIn: Boolean,
    isRefreshing: Boolean,
    onClick: (() -> Unit)? = null,
) {
    val highlighted = LocalPreferenceHighlighted.current
    val scope = rememberCoroutineScope { Dispatchers.IO }
    BasicComponent(
        modifier = modifier.highlightBackground(highlighted),
        title = tracker.name,
        summary = tracker.getDisplayUsername().takeIf { isLoggedIn && it.isNotBlank() },
        startAction = { TrackLogoIcon(tracker) },
        onClick = onClick,
        endActions = {
            if (isLoggedIn) {
                if (tracker !is EnhancedTracker) {
                    IconButton(
                        enabled = !isRefreshing,
                        minWidth = 48.dp,
                        minHeight = 48.dp,
                        onClick = {
                            scope.launch { tracker.refreshUser() }
                        },
                    ) {
                        if (!isRefreshing) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Refresh,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(32.dp),
                                contentDescription = stringResource(MR.strings.refresh_tracker_profile),
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                size = 24.dp,
                            )
                        }
                    }
                }
                Icon(
                    imageVector = MaterialSymbols.Rounded.Check,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp),
                    tint = MiuixTheme.colorScheme.primary,
                    contentDescription = stringResource(MR.strings.login_success),
                )
            }
        },
    )
}
