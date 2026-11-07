package eu.kanade.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.miuix.IconButton
import tachiyomi.presentation.core.i18n.stringResource
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * InputField in Miuix 0.9.4 has a fixed capsule and relies on its leading icon for text padding.
 * The public TextField supports the toolbar's smaller corners and an inset for the entire editor.
 */
@Composable
internal fun ToolbarSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focusRequester = remember { FocusRequester() }
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .heightIn(min = ToolbarSearchFieldDefaults.MinHeight)
            .focusRequester(focusRequester)
            .semantics { contentDescription = label },
        insideMargin = ToolbarSearchFieldDefaults.InsideMargin,
        cornerRadius = ToolbarSearchFieldDefaults.CornerRadius,
        colors = ToolbarSearchFieldDefaults.colors(),
        textStyle = MiuixTheme.textStyles.body1.copy(color = MiuixTheme.colorScheme.onSurfaceContainerHigh),
        cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
        label = label,
        useLabelAsPlaceholder = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        singleLine = true,
        interactionSource = interactionSource,
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    AppBarIcon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = stringResource(MR.strings.action_reset),
                    )
                }
            } else {
                // A supplied icon slot removes TextField's end padding, even while empty.
                Spacer(Modifier.width(ToolbarSearchFieldDefaults.InsideMargin.width))
            }
        },
    )
    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}

internal object ToolbarSearchFieldDefaults {
    val MinHeight = 48.dp
    val CornerRadius = 12.dp
    val InsideMargin = DpSize(12.dp, 8.dp)

    @Composable
    fun colors() = TextFieldDefaults.textFieldColors(
        backgroundColor = MiuixTheme.colorScheme.surfaceContainerHigh,
        labelColor = MiuixTheme.colorScheme.onSurfaceContainerHigh,
        borderColor = MiuixTheme.colorScheme.primary,
    )
}
