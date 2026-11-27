package eu.kanade.presentation.browse.components

import androidx.compose.runtime.Composable
import tachiyomi.presentation.core.components.Badge
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.glass.FolderOutline

@Composable
internal fun InLibraryBadge(enabled: Boolean) {
    if (enabled) {
        Badge(
            imageVector = MiuixIcons.Glass.FolderOutline,
        )
    }
}
