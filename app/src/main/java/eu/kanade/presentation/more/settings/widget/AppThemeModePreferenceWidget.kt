package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.kanade.domain.ui.model.ThemeMode
import tachiyomi.i18n.MR
import tachiyomi.i18n.theme_dark
import tachiyomi.i18n.theme_light
import tachiyomi.i18n.theme_system
import tachiyomi.presentation.core.components.miuix.TabRow
import tachiyomi.presentation.core.i18n.stringResource

private val options = mapOf(
    ThemeMode.SYSTEM to MR.strings.theme_system,
    ThemeMode.LIGHT to MR.strings.theme_light,
    ThemeMode.DARK to MR.strings.theme_dark,
)

@Composable
internal fun AppThemeModePreferenceWidget(
    value: ThemeMode,
    onItemClick: (ThemeMode) -> Unit,
) {
    val modes = options.keys.toList()
    TabRow(
        tabs = options.values.map { stringResource(it) },
        selectedTabIndex = modes.indexOf(value),
        onTabSelected = { onItemClick(modes[it]) },
        modifier = Modifier.padding(horizontal = PrefsHorizontalPadding, vertical = PrefsVerticalPadding),
    )
}
