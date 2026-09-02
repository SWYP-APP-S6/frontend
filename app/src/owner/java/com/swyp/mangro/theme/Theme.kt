package com.swyp.mangro.theme

import androidx.compose.runtime.Composable
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography

@Composable
fun MangroTheme(
    content: @Composable () -> Unit,
) {
    MangroTheme(
        typography = OwnerMangroTypography,
        content = content,
    )
}
