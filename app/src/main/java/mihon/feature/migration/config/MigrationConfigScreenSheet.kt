package mihon.feature.migration.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import eu.kanade.core.util.fastFilterNot
import eu.kanade.domain.source.service.SourcePreferences
import eu.kanade.presentation.components.AdaptiveSheet
import mihon.domain.migration.models.MigrationFlag
import mihon.feature.common.utils.getLabel
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.getAndSet
import tachiyomi.core.common.preference.toggle
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import tachiyomi.presentation.core.util.miuixVerticalScroll
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MigrationConfigScreenSheet(
    preferences: SourcePreferences,
    onDismissRequest: () -> Unit,
    onStartMigration: (extraSearchQuery: String?) -> Unit,
) {
    var extraSearchQuery by rememberSaveable { mutableStateOf("") }
    val migrationFlags by preferences.migrationFlags.collectAsState()
    AdaptiveSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .miuixVerticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(MR.strings.migrationConfigScreen_dataToMigrateHeader),
                    style = MiuixTheme.textStyles.subtitle,
                    modifier = Modifier.fillMaxWidth(),
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    MigrationFlag.entries
                        .fastFilterNot { it == MigrationFlag.REMOVE_DOWNLOAD }
                        .fastForEach { flag ->
                            val selected = flag in migrationFlags
                            CheckboxPreference(
                                checked = selected,
                                onCheckedChange = {
                                    preferences.migrationFlags.getAndSet { currentFlags ->
                                        if (flag in currentFlags) {
                                            currentFlags - flag
                                        } else {
                                            currentFlags + flag
                                        }
                                    }
                                },
                                title = stringResource(flag.getLabel()),
                            )
                        }
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    val removeDownloads = MigrationFlag.REMOVE_DOWNLOAD in migrationFlags
                    MigrationSheetSwitchItem(
                        title = stringResource(MR.strings.migrationConfigScreen_removeDownloadsTitle),
                        subtitle = null,
                        checked = removeDownloads,
                        onClick = {
                            preferences.migrationFlags.getAndSet {
                                if (removeDownloads) {
                                    it - MigrationFlag.REMOVE_DOWNLOAD
                                } else {
                                    it + MigrationFlag.REMOVE_DOWNLOAD
                                }
                            }
                        },
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextField(
                        value = extraSearchQuery,
                        onValueChange = { extraSearchQuery = it },
                        label = stringResource(MR.strings.migrationConfigScreen_additionalSearchQueryLabel),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(MR.strings.migrationConfigScreen_additionalSearchQuerySupportingText),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onBackgroundVariant,
                    )
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    MigrationSheetSwitchItem(
                        title = stringResource(MR.strings.migrationConfigScreen_hideUnmatchedTitle),
                        subtitle = null,
                        preference = preferences.migrationHideUnmatched,
                    )
                    MigrationSheetSwitchItem(
                        title = stringResource(MR.strings.migrationConfigScreen_hideWithoutUpdatesTitle),
                        subtitle = stringResource(MR.strings.migrationConfigScreen_hideWithoutUpdatesSubtitle),
                        preference = preferences.migrationHideWithoutUpdates,
                    )
                }
                Text(
                    text = stringResource(MR.strings.migrationConfigScreen_enhancedOptionsWarning),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.error,
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    MigrationSheetSwitchItem(
                        title = stringResource(MR.strings.migrationConfigScreen_deepSearchModeTitle),
                        subtitle = stringResource(MR.strings.migrationConfigScreen_deepSearchModeSubtitle),
                        preference = preferences.migrationDeepSearchMode,
                    )
                    MigrationSheetSwitchItem(
                        title = stringResource(MR.strings.migrationConfigScreen_prioritizeByChaptersTitle),
                        subtitle = stringResource(MR.strings.migrationConfigScreen_prioritizeByChaptersSubtitle),
                        preference = preferences.migrationPrioritizeByChapters,
                    )
                }
            }
            Button(
                onClick = {
                    val cleanedExtraSearchQuery = extraSearchQuery.trim().ifBlank { null }
                    onStartMigration(cleanedExtraSearchQuery)
                },
                colors = ButtonDefaults.buttonColorsPrimary(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
            ) {
                Text(text = stringResource(MR.strings.migrationConfigScreen_continueButtonText))
            }
        }
    }
}

@Composable
private fun MigrationSheetSwitchItem(
    title: String,
    subtitle: String?,
    preference: Preference<Boolean>,
) {
    MigrationSheetSwitchItem(
        title = title,
        subtitle = subtitle,
        checked = preference.collectAsState().value,
        onClick = { preference.toggle() },
    )
}

@Composable
private fun MigrationSheetSwitchItem(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onClick: () -> Unit,
) {
    SwitchPreference(
        title = title,
        summary = subtitle,
        checked = checked,
        onCheckedChange = { onClick() },
    )
}
