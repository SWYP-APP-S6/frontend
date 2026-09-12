package com.swyp.mangro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swyp.mangro.core.utils.HideNavigationBarWhileVisible
import com.swyp.mangro.feature.auth.navigation.Login
import com.swyp.mangro.feature.auth.navigation.authNavGraph
import com.swyp.mangro.feature.splash.navigation.Splash
import com.swyp.mangro.feature.splash.navigation.splashNavGraph

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val hideNavigationBar = currentRoute == Splash::class.qualifiedName ||
        currentRoute == Login::class.qualifiedName

    HideNavigationBarWhileVisible(hidden = hideNavigationBar)

    NavHost(
        navController = navController,
        startDestination = Splash,
    ) {
        splashNavGraph(
            navigateToLogin = {
                navController.navigate(Login) { popUpTo(Splash) { inclusive = true } }
            },
            navigateToHome = {},
        )
        authNavGraph(
            navigateToHome = {},
            navigateToPrivacyPolicy = {},
        )
    }
}
