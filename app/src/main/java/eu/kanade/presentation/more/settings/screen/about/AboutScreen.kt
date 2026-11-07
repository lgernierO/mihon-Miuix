package eu.kanade.presentation.more.settings.screen.about

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.util.LocalBackPress
import eu.kanade.presentation.util.Screen
import eu.kanade.presentation.util.rememberIs24HourSystem
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.data.updater.RELEASE_URL
import eu.kanade.tachiyomi.ui.more.NewUpdateScreen
import eu.kanade.tachiyomi.util.lang.toDateTimestampString
import eu.kanade.tachiyomi.util.system.copyToClipboard
import eu.kanade.tachiyomi.util.system.isFossBuildType
import eu.kanade.tachiyomi.util.system.isNightlyBuildType
import eu.kanade.tachiyomi.util.system.toast
import eu.kanade.tachiyomi.util.system.updaterEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import logcat.LogPriority
import mihon.app.di.appGraph
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.release.interactor.GetApplicationRelease
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.Locale
import kotlin.time.Instant

object AboutScreen : Screen() {

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val handleBack = LocalBackPress.current
        val navigator = LocalNavigator.currentOrThrow
        var isCheckingUpdates by remember { mutableStateOf(false) }
        val crashLogUtil = remember { context.appGraph.crashLogUtil }
        val is24HourSystem = rememberIs24HourSystem()
        // Date/time prefs in Appearance recreate this screen; the system clock setting can change
        // while the app is backgrounded.
        val versionName = remember(is24HourSystem) { getVersionName(withBuildDate = true) }
        val debugInfoLabel = stringResource(MR.strings.pref_debug_info)

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.pref_category_about),
                    navigateUp = if (handleBack != null) handleBack::invoke else null,
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { contentPadding ->
            AboutScreenContent(
                versionName = versionName,
                isCheckingUpdates = isCheckingUpdates,
                contentPadding = contentPadding,
                onCopyDebugInfo = {
                    context.copyToClipboard(debugInfoLabel, crashLogUtil.getDebugInfo())
                },
                onCheckForUpdates = if (updaterEnabled) {
                    {
                        if (!isCheckingUpdates) {
                            isCheckingUpdates = true
                            scope.launch {
                                checkVersion(
                                    context = context,
                                    onAvailableUpdate = { result ->
                                        navigator.push(
                                            NewUpdateScreen(
                                                versionName = result.release.version,
                                                changelogInfo = result.release.info,
                                                releaseLink = result.release.releaseLink,
                                                downloadLink = result.release.downloadLink,
                                            ),
                                        )
                                    },
                                    onFinish = { isCheckingUpdates = false },
                                )
                            }
                        }
                    }
                } else {
                    null
                },
                onOpenWhatsNew = if (BuildConfig.DEBUG) {
                    null
                } else {
                    { uriHandler.openUri(RELEASE_URL) }
                },
                onOpenLicenses = { navigator.push(OpenSourceLicensesScreen()) },
                onOpenPrivacyPolicy = { uriHandler.openUri("https://mihon.app/privacy/") },
                onOpenLink = uriHandler::openUri,
            )
        }
    }

    /**
     * Checks version and shows a user prompt if an update is available.
     */
    private suspend fun checkVersion(
        context: Context,
        onAvailableUpdate: (GetApplicationRelease.Result.NewUpdate) -> Unit,
        onFinish: () -> Unit,
    ) {
        val updateChecker = context.appGraph.updateChecker
        withContext(Dispatchers.Main) {
            try {
                when (val result = updateChecker.checkForUpdate(forceCheck = true)) {
                    is GetApplicationRelease.Result.NewUpdate -> {
                        onAvailableUpdate(result)
                    }
                    is GetApplicationRelease.Result.NoNewUpdate -> {
                        context.toast(MR.strings.update_check_no_new_updates)
                    }
                    is GetApplicationRelease.Result.OsTooOld -> {
                        context.toast(MR.strings.update_check_eol)
                    }
                }
            } catch (e: Exception) {
                context.toast(e.message)
                logcat(LogPriority.ERROR, e)
            } finally {
                onFinish()
            }
        }
    }

    fun getVersionName(withBuildDate: Boolean): String {
        return when {
            BuildConfig.DEBUG -> {
                "Debug ${BuildConfig.COMMIT_SHA}".let {
                    if (withBuildDate) {
                        "$it (${getFormattedBuildTime()})"
                    } else {
                        it
                    }
                }
            }
            isNightlyBuildType -> {
                "Nightly r${BuildConfig.COMMIT_COUNT}".let {
                    if (withBuildDate) {
                        "$it (${BuildConfig.COMMIT_SHA}, ${getFormattedBuildTime()})"
                    } else {
                        "$it (${BuildConfig.COMMIT_SHA})"
                    }
                }
            }
            else -> {
                val channel = if (isFossBuildType) "FOSS" else "Stable"
                "$channel v${BuildConfig.VERSION_NAME}".let {
                    if (withBuildDate) {
                        "$it (${getFormattedBuildTime()})"
                    } else {
                        it
                    }
                }
            }
        }
    }

    internal fun getFormattedBuildTime(): String {
        return try {
            val context = Injekt.get<Context>()
            val uiPreferences = context.appGraph.uiPreferences
            Instant.parse(BuildConfig.BUILD_TIME)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .toDateTimestampString(
                    UiPreferences.dateTimeFormat(
                        uiPreferences.dateFormat.get(),
                        uiPreferences.timeFormat.get(),
                        Locale.getDefault(),
                        DateFormat.is24HourFormat(context),
                    ),
                )
        } catch (_: Exception) {
            BuildConfig.BUILD_TIME
        }
    }
}
