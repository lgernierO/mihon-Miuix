package eu.kanade.presentation.more.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
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
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import tachiyomi.presentation.core.components.ScrollbarLazyColumn
import tachiyomi.presentation.core.components.groupedCardItems

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
    state: LazyListState = rememberLazyListState(),
) {
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
                val firstRow = sections.take(i).sumOf { it.items.size + 2 }
                val row = sections[i].items.indexOfFirst { it.title == highlightKey }
                state.animateScrollToItem(firstRow + if (row >= 0) row + 1 else 0)
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
            item(key = "section:$index:header", contentType = "header") {
                if (section.title != null) {
                    PreferenceGroupHeader(
                        title = section.title,
                        highlighted = highlightReady && section.title == highlightKey,
                    )
                } else if (index == 0) {
                    Spacer(Modifier.height(PreferenceGroupMargin))
                }
            }
            groupedCardItems(
                items = section.items,
                key = { "section:$index:${it.lazyKey()}" },
            ) { item ->
                PreferenceItem(item = item, highlightKey = highlightKey.takeIf { highlightReady })
            }
            item(key = "section:$index:footer", contentType = "spacing") {
                Spacer(Modifier.height(PreferenceGroupMargin))
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
            add(PreferenceSection(title = null, items = ungrouped.filter { it.visible }))
        }
        ungrouped.clear()
    }
    this@toSections.forEach { preference ->
        when (preference) {
            is Preference.PreferenceGroup -> {
                flushUngrouped()
                if (preference.visible && preference.preferenceItems.any { it.visible }) {
                    add(PreferenceSection(preference.title, preference.preferenceItems.filter { it.visible }))
                }
            }
            is Preference.PreferenceItem<*, *> -> ungrouped.add(preference)
        }
    }
    flushUngrouped()
}

private val PreferenceGroupMargin = 12.dp

private fun Preference.PreferenceItem<*, *>.lazyKey(): String = when (this) {
    is Preference.PreferenceItem.TrackerPreference -> "tracker:${tracker.id}"
    is Preference.PreferenceItem.SwitchPreference -> preference.key()
    is Preference.PreferenceItem.SliderPreference -> preference.key()
    is Preference.PreferenceItem.ListPreference<*> -> preference.key()
    is Preference.PreferenceItem.MultiSelectListPreference<*> -> preference.key()
    is Preference.PreferenceItem.EditTextPreference -> preference.key()
    else -> "${this::class.simpleName}:$title"
}
