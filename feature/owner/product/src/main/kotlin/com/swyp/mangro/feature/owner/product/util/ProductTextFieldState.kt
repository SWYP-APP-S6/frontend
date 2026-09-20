package com.swyp.mangro.feature.owner.product.util

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

@Composable
internal fun rememberProductTextFieldState(value: String, onValueChanged: (String) -> Unit): TextFieldState {
    val field = rememberSaveable(saver = TextFieldState.Saver) { TextFieldState(value) }
    var previousValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (previousValue != value && field.text.toString() != value) field.edit { replace(0, length, value) }
        previousValue = value
    }

    LaunchedEffect(field) {
        snapshotFlow { field.text.toString() }.collect(onValueChanged)
    }

    return field
}
