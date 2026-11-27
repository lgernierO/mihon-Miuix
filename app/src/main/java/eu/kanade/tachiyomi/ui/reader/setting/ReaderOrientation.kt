package eu.kanade.tachiyomi.ui.reader.setting

import android.content.pm.ActivityInfo
import androidx.compose.ui.graphics.vector.ImageVector
import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR
import tachiyomi.i18n.label_default
import tachiyomi.i18n.rotation_force_landscape
import tachiyomi.i18n.rotation_force_portrait
import tachiyomi.i18n.rotation_free
import tachiyomi.i18n.rotation_landscape
import tachiyomi.i18n.rotation_portrait
import tachiyomi.i18n.rotation_reverse_portrait
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.IcSmallWindowLandscape
import top.yukonga.miuix.kmp.icon.glass.LockOutline
import top.yukonga.miuix.kmp.icon.glass.Phone
import top.yukonga.miuix.kmp.icon.glass.PhoneIcon
import top.yukonga.miuix.kmp.icon.glass.RotateLeft

enum class ReaderOrientation(
    val flag: Int,
    val stringRes: StringResource,
    val icon: ImageVector,
    val flagValue: Int,
    val iconRotation: Float = 0f,
) {
    DEFAULT(
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
        MR.strings.label_default,
        MiuixIcons.Glass.PhoneIcon,
        0x00000000,
    ),
    FREE(
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
        MR.strings.rotation_free,
        MiuixIcons.Glass.RotateLeft,
        0x00000008,
    ),
    PORTRAIT(
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,
        MR.strings.rotation_portrait,
        MiuixIcons.Glass.Phone,
        0x00000010,
    ),
    LANDSCAPE(
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
        MR.strings.rotation_landscape,
        MiuixIcons.Glass.IcSmallWindowLandscape,
        0x00000018,
    ),
    LOCKED_PORTRAIT(
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
        MR.strings.rotation_force_portrait,
        MiuixIcons.Glass.LockOutline,
        0x00000020,
    ),
    LOCKED_LANDSCAPE(
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
        MR.strings.rotation_force_landscape,
        MiuixIcons.Glass.LockOutline,
        0x00000028,
        iconRotation = 90f,
    ),
    REVERSE_PORTRAIT(
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
        MR.strings.rotation_reverse_portrait,
        MiuixIcons.Glass.Phone,
        0x00000030,
        iconRotation = 180f,
    ),
    ;

    companion object {
        const val MASK = 0x00000038

        fun fromPreference(preference: Int?): ReaderOrientation = entries.find { it.flagValue == preference } ?: DEFAULT
    }
}
