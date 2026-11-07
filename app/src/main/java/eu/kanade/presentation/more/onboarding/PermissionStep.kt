package eu.kanade.presentation.more.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import eu.kanade.presentation.util.rememberRequestPackageInstallsPermissionState
import eu.kanade.tachiyomi.util.system.launchRequestPackageInstallsPermission
import eu.kanade.tachiyomi.util.system.telemetryIncluded
import mihon.app.di.appGraph
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.miuix.CompactButton
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.preference.SwitchPreference

internal class PermissionStep : OnboardingStep {

    private var notificationGranted by mutableStateOf(false)
    private var batteryGranted by mutableStateOf(false)

    override val isComplete: Boolean = true

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val privacyPreferences = remember { context.appGraph.privacyPreferences }
        val lifecycleOwner = LocalLifecycleOwner.current

        val installGranted = rememberRequestPackageInstallsPermissionState()

        DisposableEffect(lifecycleOwner.lifecycle) {
            val observer = object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                            PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }
                    batteryGranted = context.getSystemService<PowerManager>()!!
                        .isIgnoringBatteryOptimizations(context.packageName)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Column {
            PermissionCheckbox(
                title = stringResource(MR.strings.onboarding_permission_install_apps),
                subtitle = stringResource(MR.strings.onboarding_permission_install_apps_description),
                granted = installGranted,
                onButtonClick = {
                    context.launchRequestPackageInstallsPermission()
                },
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionRequester = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = {
                        // no-op. resulting checks is being done on resume
                    },
                )
                PermissionCheckbox(
                    title = stringResource(MR.strings.onboarding_permission_notifications),
                    subtitle = stringResource(MR.strings.onboarding_permission_notifications_description),
                    granted = notificationGranted,
                    onButtonClick = { permissionRequester.launch(Manifest.permission.POST_NOTIFICATIONS) },
                )
            }

            PermissionCheckbox(
                title = stringResource(MR.strings.onboarding_permission_ignore_battery_opts),
                subtitle = stringResource(MR.strings.onboarding_permission_ignore_battery_opts_description),
                granted = batteryGranted,
                onButtonClick = {
                    @SuppressLint("BatteryLife")
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    context.startActivity(intent)
                },
            )

            if (!telemetryIncluded) return@Column

            val crashlyticsPref = privacyPreferences.crashlytics
            val crashlytics by crashlyticsPref.collectAsState()
            PermissionSwitch(
                title = stringResource(MR.strings.onboarding_permission_crashlytics),
                subtitle = stringResource(MR.strings.onboarding_permission_crashlytics_description),
                granted = crashlytics,
                onToggleChange = crashlyticsPref::set,
            )

            val analyticsPref = privacyPreferences.analytics
            val analytics by analyticsPref.collectAsState()
            PermissionSwitch(
                title = stringResource(MR.strings.onboarding_permission_analytics),
                subtitle = stringResource(MR.strings.onboarding_permission_analytics_description),
                granted = analytics,
                onToggleChange = analyticsPref::set,
            )
        }
    }

    @Composable
    private fun PermissionCheckbox(
        title: String,
        subtitle: String,
        granted: Boolean,
        modifier: Modifier = Modifier,
        onButtonClick: () -> Unit,
    ) {
        BasicComponent(
            modifier = modifier.semantics(mergeDescendants = true) {
                if (granted) {
                    role = Role.Checkbox
                    toggleableState = ToggleableState.On
                }
            },
            title = title,
            summary = subtitle,
            endActions = {
                if (granted) {
                    Checkbox(
                        state = ToggleableState.On,
                        onClick = null,
                    )
                } else {
                    CompactButton(onClick = onButtonClick) {
                        Text(stringResource(MR.strings.onboarding_permission_action_grant))
                    }
                }
            },
        )
    }

    @Composable
    private fun PermissionSwitch(
        title: String,
        subtitle: String,
        granted: Boolean,
        modifier: Modifier = Modifier,
        onToggleChange: (Boolean) -> Unit,
    ) {
        SwitchPreference(
            modifier = modifier,
            title = title,
            summary = subtitle,
            checked = granted,
            onCheckedChange = onToggleChange,
        )
    }
}
