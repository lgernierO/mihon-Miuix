package tachiyomi.presentation.core.util

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Hold click intent until the pager arrives; swipes follow the nearest actual page. */
@Stable
class PagerTabSelection internal constructor(
    private val pagerState: PagerState,
    private val scope: CoroutineScope,
) {
    private var requestedPage by mutableStateOf<Int?>(null)
    private var navigationJob: Job? = null
    private var generation = 0

    val selectedIndex: Int
        get() = requestedPage ?: pagerState.currentPage

    fun select(page: Int) {
        if (page !in 0 until pagerState.pageCount) return
        val request = ++generation
        navigationJob?.cancel()
        requestedPage = page
        navigationJob = scope.launch(start = CoroutineStart.LAZY) {
            try {
                if (pagerState.pageCount == 2) {
                    pagerState.animateScrollToPage(page, animationSpec = tween(300, easing = EaseInOut))
                } else {
                    pagerState.scrollToPage(page)
                }
            } finally {
                // A cancelled older request cannot release a newer click's selection.
                if (request == generation) {
                    requestedPage = null
                    navigationJob = null
                }
            }
        }
        navigationJob?.start()
    }

    fun cancel() {
        generation++
        navigationJob?.cancel()
        navigationJob = null
        requestedPage = null
    }
}

@Composable
fun rememberPagerTabSelection(pagerState: PagerState, pageKeys: List<Any>): PagerTabSelection {
    val scope = rememberCoroutineScope()
    val selection = remember(pagerState, pageKeys) { PagerTabSelection(pagerState, scope) }
    DisposableEffect(selection) {
        onDispose { selection.cancel() }
    }
    LaunchedEffect(selection, pagerState) {
        pagerState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) selection.cancel()
        }
    }
    return selection
}
