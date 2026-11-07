package tachiyomi.presentation.core.components.miuix

import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
@Suppress("DEPRECATION")
fun MiuixPopupSystemBars(
    backgroundColor: Color = MiuixTheme.colorScheme.background,
    windowInsets: MutableWindowInsets? = null,
) {
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window ?: return
    val controller = remember(window) { WindowInsetsControllerCompat(window, window.decorView) }
    val lightSystemBars = backgroundColor.luminance() > 0.5f

    DisposableEffect(view, window, controller, windowInsets) {
        val navigationBarColor = window.navigationBarColor
        val statusBarColor = window.statusBarColor
        val systemBarFlags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        val originalFlags = window.attributes.flags
        val lightNavigationBars = controller.isAppearanceLightNavigationBars
        val lightStatusBars = controller.isAppearanceLightStatusBars
        val navigationBarContrast = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced
        } else {
            false
        }
        val statusBarContrast = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced
        } else {
            false
        }
        val navigationBarDividerColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor
        } else {
            android.graphics.Color.TRANSPARENT
        }
        val dialogView = view.parent as? View ?: view
        var disposed = false
        var requestPending = false
        var lastInsets: Insets? = null
        val updateInsets = {
            val rootInsets = ViewCompat.getRootWindowInsets(window.decorView)
                ?: ViewCompat.getRootWindowInsets(dialogView)
                ?: ViewCompat.getRootWindowInsets(view)
            val currentInsets = rootInsets?.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            if (currentInsets != null && currentInsets != lastInsets) {
                lastInsets = currentInsets
                windowInsets?.insets = WindowInsets(
                    currentInsets.left,
                    currentInsets.top,
                    currentInsets.right,
                    currentInsets.bottom,
                )
                true
            } else {
                false
            }
        }
        val requestInsets = Runnable {
            requestPending = false
            if (!disposed && view.isAttachedToWindow && view.width > 0 && view.height > 0) {
                updateInsets()
                ViewCompat.requestApplyInsets(dialogView)
                if (dialogView !== view) ViewCompat.requestApplyInsets(view)
            }
        }
        val scheduleInsets = {
            if (!disposed && !requestPending) {
                requestPending = true
                view.post(requestInsets)
            }
        }
        val layoutListener = View.OnLayoutChangeListener {
                _,
                left,
                top,
                right,
                bottom,
                oldLeft,
                oldTop,
                oldRight,
                oldBottom,
            ->
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                scheduleInsets()
            }
        }
        val attachListener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(attachedView: View) {
                scheduleInsets()
            }

            override fun onViewDetachedFromWindow(detachedView: View) = Unit
        }
        val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            if (!disposed && view.isAttachedToWindow && updateInsets()) {
                scheduleInsets()
            }
        }
        val viewTreeObserver = view.viewTreeObserver
        view.addOnLayoutChangeListener(layoutListener)
        if (dialogView !== view) dialogView.addOnLayoutChangeListener(layoutListener)
        view.addOnAttachStateChangeListener(attachListener)
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        scheduleInsets()
        onDispose {
            disposed = true
            view.removeCallbacks(requestInsets)
            view.removeOnLayoutChangeListener(layoutListener)
            if (dialogView !== view) dialogView.removeOnLayoutChangeListener(layoutListener)
            view.removeOnAttachStateChangeListener(attachListener)
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            } else {
                view.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            }
            window.addFlags(originalFlags and systemBarFlags)
            window.clearFlags(originalFlags.inv() and systemBarFlags)
            window.navigationBarColor = navigationBarColor
            window.statusBarColor = statusBarColor
            controller.isAppearanceLightNavigationBars = lightNavigationBars
            controller.isAppearanceLightStatusBars = lightStatusBars
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = navigationBarContrast
                window.isStatusBarContrastEnforced = statusBarContrast
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.navigationBarDividerColor = navigationBarDividerColor
            }
        }
    }
    SideEffect {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
        )
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        controller.isAppearanceLightNavigationBars = lightSystemBars
        controller.isAppearanceLightStatusBars = lightSystemBars
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = android.graphics.Color.TRANSPARENT
        }
    }
}
