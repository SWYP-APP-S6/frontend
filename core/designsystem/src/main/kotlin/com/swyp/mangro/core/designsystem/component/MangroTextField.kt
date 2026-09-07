package com.swyp.mangro.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.Orange600

@Composable
fun MangroTextField(
    state: TextFieldState,
    placeholder: String,
    modifier: Modifier = Modifier,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: ((ImeAction) -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = if (isFocused) {
        Orange600
    } else {
        MangroTheme.colors.borderDefault
    }

    val shape = RoundedCornerShape(8.dp)

    BasicTextField(
        state = state,
        interactionSource = interactionSource,
        textStyle = MangroTheme.typography.body.bodyM.copy(
            color = MangroTheme.colors.textTitle,
        ),
        lineLimits = lineLimits,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction?.let { handler ->
            KeyboardActionHandler { performDefaultAction ->
                handler(keyboardOptions.imeAction)
                performDefaultAction()
            }
        },
        inputTransformation = inputTransformation,
        outputTransformation = outputTransformation,
        modifier = modifier,
        decorator = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MangroTheme.colors.textOnBrandWhite)
                    .border(
                        width = if (isFocused) 1.2.dp else 1.dp,
                        color = borderColor,
                        shape = shape,
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (state.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MangroTheme.typography.body.bodyM,
                        color = MangroTheme.colors.textCanceled,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun MangroTextFieldPreview() {
    MangroTheme {
        Column(
            modifier = Modifier
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MangroTextField(
                state = remember { TextFieldState() },
                placeholder = "Text",
            )

            MangroTextField(
                state = remember { TextFieldState(initialText = "Text") },
                placeholder = "Text",
            )
        }
    }
}
