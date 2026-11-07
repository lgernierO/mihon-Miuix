package eu.kanade.presentation.manga.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tachiyomi.presentation.core.components.miuix.Text

@Composable
fun DotSeparatorText(
    modifier: Modifier = Modifier,
) {
    Text(
        text = " • ",
        modifier = modifier,
    )
}
