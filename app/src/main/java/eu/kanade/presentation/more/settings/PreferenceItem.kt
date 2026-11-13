package eu.kanade.presentation.more.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.widget.EditTextPreferenceWidget
import eu.kanade.presentation.more.settings.widget.InfoWidget
import eu.kanade.presentation.more.settings.widget.ListPreferenceWidget
import eu.kanade.presentation.more.settings.widget.MultiSelectListPreferenceWidget
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TrackingPreferenceWidget
import eu.kanade.presentation.more.settings.widget.highlightBackground
import eu.kanade.tachiyomi.data.track.RefreshResult
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.i18n.refresh_tracker_error
import tachiyomi.i18n.refresh_tracker_success
import tachiyomi.presentation.core.util.collectAsState
import tachiyomi.presentation.core.util.sliderValue
import top.yukonga.miuix.kmp.preference.SliderPreference
import kotlin.time.Duration.Companion.seconds

val LocalPreferenceHighlighted = compositionLocalOf(structuralEqualityPolicy()) { false }
val LocalPreferenceMinHeight = compositionLocalOf(structuralEqualityPolicy()) { 56.dp }

@Composable
fun StatusWrapper(
    item: Preference.PreferenceItem<*, *>,
    highlightKey: String?,
    content: @Composable () -> Unit,
) {
    val enabled = item.visible
    val highlighted = item.title == highlightKey
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(highlighted, enabled) {
        if (highlighted && enabled) {
            // The list first scrolls to the containing Card, then reveals this row.
            delay(0.75.seconds)
            bringIntoViewRequester.bringIntoView()
        }
    }
    AnimatedVisibility(
        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
        visible = enabled,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        content = {
            CompositionLocalProvider(
                LocalPreferenceHighlighted provides highlighted,
                content = content,
            )
        },
    )
}

@Composable
internal fun PreferenceItem(
    item: Preference.PreferenceItem<*, *>,
    highlightKey: String?,
) {
    val scope = rememberCoroutineScope()
    StatusWrapper(
        item = item,
        highlightKey = highlightKey,
    ) {
        when (item) {
            is Preference.PreferenceItem.SwitchPreference -> {
                val value by item.preference.collectAsState()
                SwitchPreferenceWidget(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    checked = value,
                    onCheckedChanged = { newValue ->
                        scope.launch {
                            if (item.onValueChanged(newValue)) {
                                item.preference.set(newValue)
                            }
                        }
                    },
                )
            }
            is Preference.PreferenceItem.SliderPreference -> {
                val value by item.preference.collectAsState()
                val minimum = minOf(item.valueRange.first, item.valueRange.last)
                val maximum = maxOf(item.valueRange.first, item.valueRange.last)
                SliderPreference(
                    modifier = Modifier.highlightBackground(LocalPreferenceHighlighted.current),
                    value = value.toFloat().coerceIn(minimum.toFloat(), maximum.toFloat()),
                    valueRange = minimum.toFloat()..if (minimum < maximum) maximum.toFloat() else minimum + 1f,
                    steps = item.steps.coerceAtLeast(0),
                    enabled = minimum < maximum,
                    reverseDirection = item.valueRange.step < 0,
                    title = item.title,
                    summary = item.subtitle,
                    valueText = item.valueText(value),
                    onValueChange = { newValue ->
                        val intValue = item.valueRange.sliderValue(newValue)
                        scope.launch {
                            if (intValue != item.preference.get() && item.onValueChanged(intValue)) {
                                item.preference.set(intValue)
                            }
                        }
                    },
                )
            }
            is Preference.PreferenceItem.ListPreference<*> -> {
                val value by item.preference.collectAsState()
                ListPreferenceWidget(
                    value = value,
                    title = item.title,
                    subtitle = item.internalSubtitleProvider(value, item.entries),
                    icon = item.icon,
                    entries = item.entries,
                    onValueChange = { newValue ->
                        scope.launch {
                            if (item.internalOnValueChanged(newValue!!)) {
                                item.internalSet(newValue)
                            }
                        }
                    },
                )
            }
            is Preference.PreferenceItem.BasicListPreference -> {
                ListPreferenceWidget(
                    value = item.value,
                    title = item.title,
                    subtitle = item.subtitleProvider(item.value, item.entries),
                    icon = item.icon,
                    entries = item.entries,
                    onValueChange = { scope.launch { item.onValueChanged(it) } },
                )
            }
            is Preference.PreferenceItem.MultiSelectListPreference<*> -> {
                val values by item.preference.collectAsState()
                MultiSelectListPreferenceWidget(
                    values = values,
                    title = item.title,
                    subtitle = item.internalSubtitleProvider(values, item.entries),
                    icon = item.icon,
                    entries = item.entries,
                    onValuesChange = { newValues ->
                        scope.launch {
                            if (item.internalOnValueChanged(newValues)) {
                                item.internalSet(newValues)
                            }
                        }
                    },
                )
            }
            is Preference.PreferenceItem.TextPreference -> {
                TextPreferenceWidget(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    widget = item.widget,
                    onPreferenceClick = item.onClick,
                )
            }
            is Preference.PreferenceItem.EditTextPreference -> {
                val values by item.preference.collectAsState()
                EditTextPreferenceWidget(
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    value = values,
                    onConfirm = {
                        val accepted = item.onValueChanged(it)
                        if (accepted) item.preference.set(it)
                        accepted
                    },
                )
            }
            is Preference.PreferenceItem.TrackerPreference -> {
                val context = LocalContext.current
                LaunchedEffect(item.tracker) {
                    item.tracker.refreshResultFlow.collect {
                        when (it) {
                            RefreshResult.Success -> context.toast(
                                context.stringResource(MR.strings.refresh_tracker_success, item.tracker.name),
                            )

                            is RefreshResult.Error -> context.toast(
                                context.stringResource(MR.strings.refresh_tracker_error, item.tracker.name, it.msg),
                            )
                        }
                    }
                }
                val isLoggedIn by item.tracker.let { tracker ->
                    tracker.isLoggedInFlow.collectAsState(tracker.isLoggedIn)
                }
                val isRefreshing by item.tracker.isRefreshingFlow.collectAsState()
                TrackingPreferenceWidget(
                    tracker = item.tracker,
                    isLoggedIn = isLoggedIn,
                    isRefreshing = isRefreshing,
                    onClick = { if (isLoggedIn) item.logout() else item.login() },
                )
            }
            is Preference.PreferenceItem.InfoPreference -> {
                InfoWidget(text = item.title)
            }
            is Preference.PreferenceItem.CustomPreference -> {
                item.content()
            }
        }
    }
}
