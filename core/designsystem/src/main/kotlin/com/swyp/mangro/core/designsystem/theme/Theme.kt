package com.swyp.mangro.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MangroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MangroDarkColorScheme else MangroLightColorScheme,
        typography = MangroTypography,
        shapes = MangroShapes,
        content = content,
    )
}
