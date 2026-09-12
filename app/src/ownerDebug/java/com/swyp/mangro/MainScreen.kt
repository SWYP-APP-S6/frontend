package com.swyp.mangro

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.onboarding.navigation.OwnerOnboardingNavigation

/** Address search uses Kakao; categories and registration submission remain Debug fixtures. */
@Composable
internal fun MainScreen() {
    val activity = LocalActivity.current
    val navController = rememberNavController()

    MangroTheme(typography = OwnerMangroTypography) {
        OwnerOnboardingNavigation(
            navController = navController,
            onComplete = { activity?.finish() },
            onBack = { activity?.finish() },
        )
    }
}
