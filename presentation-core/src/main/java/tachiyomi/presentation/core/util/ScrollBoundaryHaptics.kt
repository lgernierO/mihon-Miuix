package tachiyomi.presentation.core.util

import kotlin.math.abs
import kotlin.math.sign

/**
 * One light feedback per reached edge within a gesture. A new gesture or moving into content rearms it.
 * Tracks actual unconsumed scroll, so short content and a refresh parent work like ordinary lists.
 */
internal class ScrollBoundaryHaptics {
    private var reachedEdge = 0
    private var pendingEdge = 0
    private var pendingDistance = 0f

    fun onScroll(consumed: Float, available: Float, threshold: Float): Boolean {
        if (reachedEdge != 0 && consumed * reachedEdge < -1f) {
            reachedEdge = 0
        }
        if (abs(available) < 1f) {
            if (consumed != 0f) {
                pendingEdge = 0
                pendingDistance = 0f
            }
            return false
        }

        val edge = available.sign.toInt()
        if (edge != pendingEdge) {
            pendingEdge = edge
            pendingDistance = 0f
        }
        pendingDistance += abs(available)
        if (edge == reachedEdge || pendingDistance < threshold) return false
        reachedEdge = edge
        return true
    }

    fun onFling(available: Float): Boolean {
        if (abs(available) <= 1f) return false
        val edge = available.sign.toInt()
        if (edge == reachedEdge) return false
        reachedEdge = edge
        pendingEdge = edge
        pendingDistance = 0f
        return true
    }

    fun reset() {
        reachedEdge = 0
        pendingEdge = 0
        pendingDistance = 0f
    }
}
