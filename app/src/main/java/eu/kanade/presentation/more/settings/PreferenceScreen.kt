package eu.kanade.presentation.more.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.screen.SearchableSettings
import eu.kanade.presentation.more.settings.widget.PreferenceGroupHeader
import kotlinx.coroutines.delay
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import top.yukonga.miuix.kmp.basic.Card
import kotlin.time.Duration.Companion.seconds

/**
 * Preference Screen composable which contains a list of [Preference] items
 * @param items [Preference] items which should be displayed on the preference screen. An item can be a single [PreferenceItem] or a group ([Preference.PreferenceGroup])
 * @param modifier [Modifier] to be applied to the preferenceScreen layout
 */
@Composable
fun PreferenceScreen(
    items: List<Preference>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val state = rememberLazyListState()
    val highlightKey = remember { SearchableSettings.highlightKey }
    var highlightReady by remember { mutableStateOf(false) }
    val sections = items.toSections()
    if (highlightKey != null) {
        LaunchedEffect(highlightKey) {
            delay(0.5.seconds)
            val i = sections.indexOfFirst { section ->
                section.title == highlightKey || section.items.any { it.visible && it.title == highlightKey }
            }
            if (i >= 0) {
                state.animateScrollToItem(i)
            }
            highlightReady = true
            SearchableSettings.highlightKey = null
        }
    }

    ScrollbarLazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
    ) {
        sections.forEachIndexed { index, section ->
            item {
                Column {
                    if (index == 0 && section.title == null) {
                        Spacer(modifier = Modifier.height(PreferenceGroupMargin))
                    }
                    if (section.title != null) {
                        PreferenceGroupHeader(
                            title = section.title,
                            highlighted = highlightReady && section.title == highlightKey,
                        )
                    }
                    Card(
                        modifier = Modifier.padding(horizontal = PreferenceGroupMargin),
                    ) {
                        section.items.forEach { item ->
                            PreferenceItem(item = item, highlightKey = highlightKey.takeIf { highlightReady })
                        }
                    }
                    Spacer(modifier = Modifier.height(PreferenceGroupMargin))
                }
            }
        }
    }
}

private data class PreferenceSection(
    val title: String?,
    val items: List<Preference.PreferenceItem<*, *>>,
)

private fun List<Preference>.toSections(): List<PreferenceSection> = buildList {
    val ungrouped = mutableListOf<Preference.PreferenceItem<*, *>>()
    fun flushUngrouped() {
        if (ungrouped.any { it.visible }) {
            add(PreferenceSection(title = null, items = ungrouped.toList()))
        }
        ungrouped.clear()
    }
    this@toSections.forEach { preference ->
        when (preference) {
            is Preference.PreferenceGroup -> {
                flushUngrouped()
                if (preference.visible && preference.preferenceItems.any { it.visible }) {
                    add(PreferenceSection(preference.title, preference.preferenceItems))
                }
            }
            is Preference.PreferenceItem<*, *> -> ungrouped.add(preference)
        }
    }
    flushUngrouped()
}

private val PreferenceGroupMargin = 12.dp
