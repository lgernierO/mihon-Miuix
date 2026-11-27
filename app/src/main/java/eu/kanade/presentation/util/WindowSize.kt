package eu.kanade.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import eu.kanade.domain.ui.model.TabletUiMode
import mihon.app.di.appGraph
import tachiyomi.presentation.core.components.miuix.LocalContainerSize
import tachiyomi.presentation.core.util.collectAsState

@Composable
fun isTabletUi(): Boolean {
    val mode by LocalContext.current.appGraph.uiPreferences.tabletUiMode.collectAsState()
    val size = LocalContainerSize.current ?: LocalWindowInfo.current.containerSize
    val landscape = size.width > size.height
    val width = with(LocalDensity.current) { size.width.toDp() }
    return when (mode) {
        TabletUiMode.AUTOMATIC -> width.value >= if (landscape) 600 else 700
        TabletUiMode.ALWAYS -> true
        TabletUiMode.LANDSCAPE -> landscape
        TabletUiMode.NEVER -> false
    }
}
