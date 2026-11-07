package tachiyomi.presentation.core.util

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Velocity
import top.yukonga.miuix.kmp.utils.overScrollVertical
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Miuix owns the spring. Observe the child's boundary before the spring or a refresh parent
 * consumes it. Unlike Miuix's fling-only haptic, this also covers dragging and short pages.
 * Lazy containers must set overscrollEffect = null to avoid a second spring.
 */
fun Modifier.miuixScroll(enabled: Boolean = true): Modifier =
    if (enabled) overScrollVertical().then(ScrollBoundaryHapticElement) else this

/**
 * The same page pattern for ordinary columns, with Foundation's second overscroll effect disabled.
 */
fun Modifier.miuixVerticalScroll(state: ScrollState, enabled: Boolean = true): Modifier =
    miuixScroll(enabled).verticalScroll(state = state, enabled = enabled, overscrollEffect = null)

private object ScrollBoundaryHapticElement : ModifierNodeElement<ScrollBoundaryHapticNode>() {
    override fun create() = ScrollBoundaryHapticNode()

    override fun update(node: ScrollBoundaryHapticNode) = Unit

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = javaClass.hashCode()

    override fun InspectorInfo.inspectableProperties() {
        name = "scrollBoundaryHaptic"
    }
}

private class ScrollBoundaryHapticNode :
    DelegatingNode(),
    CompositionLocalConsumerModifierNode,
    NestedScrollConnection {
    private val boundaries = ScrollBoundaryHaptics()
    private var directGesture = false
    private var hadDirectGesture = false
    private var flingActive = false
    private var lastDirectInput: TimeMark? = null

    init {
        delegate(
            SuspendingPointerInputModifierNode {
                try {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val active = when (event.type) {
                                PointerEventType.PanStart, PointerEventType.PanMove -> true
                                PointerEventType.PanEnd -> false
                                PointerEventType.Scroll -> directGesture
                                else -> event.changes.any { it.pressed }
                            }
                            if (active && !directGesture) {
                                // Every new edge pull works on short content; holding one edge
                                // and its release fling still share a single feedback.
                                resetGesture()
                            }
                            directGesture = active
                        }
                    }
                } finally {
                    resetGesture()
                    directGesture = false
                }
            },
        )
    }

    override fun onAttach() {
        delegate(nestedScrollModifierNode(this, null))
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (directGesture && source == NestedScrollSource.UserInput && (consumed.y != 0f || available.y != 0f)) {
            hadDirectGesture = true
            lastDirectInput = TimeSource.Monotonic.markNow()
        }
        if (
            isAttached &&
            directGesture &&
            source == NestedScrollSource.UserInput &&
            boundaries.onScroll(
                consumed = consumed.y,
                available = available.y,
                threshold = currentValueOf(LocalViewConfiguration).touchSlop,
            )
        ) {
            feedback()
        }
        return Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        // A fling must belong to this gesture; cancelled drags cannot arm a later programmatic one.
        flingActive = hadDirectGesture && lastDirectInput?.elapsedNow()?.let { it < 250.milliseconds } == true
        hadDirectGesture = false
        lastDirectInput = null
        return Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (isAttached && flingActive && boundaries.onFling(available.y)) {
            feedback()
        }
        flingActive = false
        return Velocity.Zero
    }

    private fun feedback() {
        currentValueOf(LocalHapticFeedback).performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    override fun onDetach() {
        resetGesture()
        directGesture = false
        super.onDetach()
    }

    private fun resetGesture() {
        boundaries.reset()
        hadDirectGesture = false
        flingActive = false
        lastDirectInput = null
    }
}
