package com.swyp.mangro.core.designsystem.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun MangroInputBox(
    label: String,
    hint: String,
    state: TextFieldState,
    placeholder: String,
    modifier: Modifier = Modifier,
    isRequired: Boolean = true,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: ((ImeAction) -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = label,
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.heading.headingXXS,
            )

            Spacer(modifier = Modifier.width(2.dp))

            if (isRequired) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_star),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = hint,
            color = MangroTheme.colors.textSubtitle,
            style = MangroTheme.typography.caption.captionS,
        )

        Spacer(modifier = Modifier.height(12.dp))

        MangroTextField(
            state = state,
            placeholder = placeholder,
            lineLimits = lineLimits,
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            inputTransformation = inputTransformation,
            outputTransformation = outputTransformation,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MangroLabeledTextFieldPreview() {
    val focusManager = LocalFocusManager.current

    MangroTheme {
        Box(
            modifier = Modifier
                .padding(20.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                MangroInputBox(
                    label = "Label",
                    hint = "Hint message",
                    state = remember { TextFieldState() },
                    placeholder = "Text",
                )

                MangroInputBox(
                    label = "Label",
                    hint = "Hint message",
                    state = remember { TextFieldState(initialText = "입력된 텍스트") },
                    placeholder = "Text",
                )
            }
        }
    }
}
