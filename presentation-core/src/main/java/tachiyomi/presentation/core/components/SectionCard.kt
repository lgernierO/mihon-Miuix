package tachiyomi.presentation.core.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle

@Composable
fun LazyItemScope.SectionCard(
    titleRes: StringResource? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (titleRes != null) {
        SmallTitle(
            modifier = Modifier.semantics { heading() },
            text = stringResource(titleRes),
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = SectionCardMargin,
                top = if (titleRes == null) SectionCardMargin else 0.dp,
                end = SectionCardMargin,
                bottom = SectionCardMargin,
            ),
        insideMargin = contentPadding,
        content = content,
    )
}

private val SectionCardMargin = 12.dp
