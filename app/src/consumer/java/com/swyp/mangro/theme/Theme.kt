package com.swyp.mangro.theme

import androidx.compose.runtime.Composable
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroTypography
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun MangroTheme(
    content: @Composable () -> Unit,
) {
    MangroTheme(
        typography = ConsumerMangroTypography,
        content = content,
    )
}
