package tachiyomi.presentation.core.components.miuix

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.Snackbar
import top.yukonga.miuix.kmp.basic.SnackbarData
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.SnackbarHost as MiuixSnackbarHost

@Composable
fun SnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { Snackbar(it) },
) {
    MiuixSnackbarHost(
        state = hostState,
        modifier = modifier,
        content = snackbar,
    )
}
