package com.swyp.mangro.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object MangroTheme {
    val colors: MangroColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMangroColors.current
}

@Composable
fun ProvideMangroColors(
    colors: MangroColors,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMangroColors provides colors,
        content = content,
    )
}

@Composable
fun MangroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) defaultMangroColors else defaultMangroColors

    val colorScheme = if (darkTheme) {
        darkColorScheme(background = colors.surfaceNormal)
    } else {
        lightColorScheme(background = colors.surfaceNormal)
    }

    ProvideMangroColors(colors = colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
