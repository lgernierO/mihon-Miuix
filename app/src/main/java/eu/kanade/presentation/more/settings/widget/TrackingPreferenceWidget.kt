package eu.kanade.presentation.more.settings.widget

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
import tachiyomi.i18n.MR
import tachiyomi.i18n.login_success
import tachiyomi.i18n.refresh_tracker_profile
import tachiyomi.presentation.core.components.miuix.CompactActionIcon
import tachiyomi.presentation.core.components.miuix.CompactIconButton
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.Ok
import top.yukonga.miuix.kmp.icon.glass.Refresh
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
                    CompactIconButton(
                        enabled = !isRefreshing,
                        onClick = {
                            scope.launch { tracker.refreshUser() }
                        },
                    ) {
                        if (!isRefreshing) {
                            CompactActionIcon(
                                imageVector = MiuixIcons.Glass.Refresh,
                                contentDescription = stringResource(MR.strings.refresh_tracker_profile),
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                size = 20.dp,
                            )
                        }
                    }
                }
                PreferenceIcon(
                    imageVector = MiuixIcons.Glass.Ok,
                    tint = MiuixTheme.colorScheme.primary,
                    contentDescription = stringResource(MR.strings.login_success),
                )
            }
        },
    )
}
