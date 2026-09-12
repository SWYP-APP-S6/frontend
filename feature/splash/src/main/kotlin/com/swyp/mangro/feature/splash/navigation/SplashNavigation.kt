package com.swyp.mangro.feature.splash.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.splash.SplashRoute
import kotlinx.serialization.Serializable

@Serializable
data object Splash

fun NavGraphBuilder.splashNavGraph(
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit,
) {
    composable<Splash> {
        SplashRoute(
            navigateToLogin = navigateToLogin,
            navigateToHome = navigateToHome,
        )
    }
}
