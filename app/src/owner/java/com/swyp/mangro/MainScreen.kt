package com.swyp.mangro

import androidx.compose.runtime.Composable
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeScreenRoute
import com.swyp.mangro.theme.MangroTheme

@Composable
internal fun MainScreen() {
    MangroTheme {
        OwnerHomeScreenRoute()
    }
}
