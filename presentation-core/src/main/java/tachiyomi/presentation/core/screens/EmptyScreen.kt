package tachiyomi.presentation.core.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import tachiyomi.presentation.core.components.ActionButton
import tachiyomi.presentation.core.components.miuix.Text
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.miuixVerticalScroll
import tachiyomi.presentation.core.util.secondaryItemAlpha
import kotlin.random.Random

data class EmptyScreenAction(
    val stringRes: StringResource,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun EmptyScreen(
    stringRes: StringResource,
    modifier: Modifier = Modifier,
    actions: List<EmptyScreenAction>? = null,
) {
    EmptyScreen(
        message = stringResource(stringRes),
        modifier = modifier,
        actions = actions,
    )
}

@Composable
fun EmptyScreen(
    message: String,
    modifier: Modifier = Modifier,
    actions: List<EmptyScreenAction>? = null,
) {
    val face = remember { getRandomErrorFace() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .miuixVerticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = face,
                modifier = Modifier.secondaryItemAlpha(),
                style = MaterialTheme.typography.displayMedium,
            )
        }

        Text(
            text = message,
            modifier = Modifier
                .paddingFromBaseline(top = 24.dp)
                .secondaryItemAlpha(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        if (!actions.isNullOrEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                actions.forEach {
                    ActionButton(
                        title = stringResource(it.stringRes),
                        icon = it.icon,
                        onClick = it.onClick,
                    )
                }
            }
        }
    }
}

private val ErrorFaces = listOf(
    "(･o･;)",
    "Σ(ಠ_ಠ)",
    "ಥ_ಥ",
    "(˘･_･˘)",
    "(；￣Д￣)",
    "(･Д･。",
    "(╬ಠ益ಠ)",
    "(╥﹏╥)",
    "(⋟﹏⋞)",
    "Ò︵Ó",
    " ˙ᯅ˙)",
    "(¬_¬)",
)

private fun getRandomErrorFace(): String {
    return ErrorFaces[Random.nextInt(ErrorFaces.size)]
}
