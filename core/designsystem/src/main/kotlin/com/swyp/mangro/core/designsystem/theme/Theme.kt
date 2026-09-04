package com.swyp.mangro.core.designsystem.theme

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

object MangroTheme {
    val colors: MangroColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMangroColors.current

    val typography: MangroTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalMangroTypography.current
}

@Composable
fun ProvideMangroColorsAndTypography(
    colors: MangroColors,
    typography: MangroTypography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMangroColors provides colors,
        LocalMangroTypography provides typography,
        content = content,
    )
}

@Composable
fun MangroTheme(
    typography: MangroTypography = ConsumerMangroTypography,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val activity = LocalActivity.current
    SideEffect {
        activity?.window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = true
        }
    }

    val colors = defaultMangroColors

    val colorScheme = if (darkTheme) {
        darkColorScheme(background = colors.surfaceNormal)
    } else {
        lightColorScheme(background = colors.surfaceNormal)
    }

    ProvideMangroColorsAndTypography(colors = colors, typography = typography) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
