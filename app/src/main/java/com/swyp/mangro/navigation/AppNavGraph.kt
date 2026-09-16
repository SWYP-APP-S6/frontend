package com.swyp.mangro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.utils.HideNavigationBarWhileVisible
import com.swyp.mangro.feature.auth.navigation.Login
import com.swyp.mangro.feature.auth.navigation.authNavGraph
import com.swyp.mangro.feature.consumer.hold.navigation.holdHistoryScreen
import com.swyp.mangro.feature.consumer.hold.navigation.holdScreen
import com.swyp.mangro.feature.consumer.hold.navigation.navigateToHold
import com.swyp.mangro.feature.consumer.hold.navigation.navigateToHoldHistory
import com.swyp.mangro.feature.consumer.home.navigation.Home
import com.swyp.mangro.feature.consumer.home.navigation.homeNavGraph
import com.swyp.mangro.feature.consumer.store.navigation.productDetailScreen
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

    val onNavigateToMenu: (ConsumerMenu) -> Unit = { menu ->
        when (menu) {
            ConsumerMenu.HOME -> {
                navController.navigate(Home) { popUpTo(Home) { inclusive = true } }
            }
            ConsumerMenu.WISH_LIST -> {
                navController.navigateToHoldHistory()
            }
            ConsumerMenu.MY -> { }
        }
    }

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
            navController = navController,
            navigateToHome = { navController.navigate(Home) },
            navigateToPrivacyPolicy = {},
        )
        homeNavGraph(
            navController = navController,
            onNavigateToMenu = onNavigateToMenu,
        )
        productDetailScreen(
            navController = navController,
            onNavigateToHold = { holdId -> navController.navigateToHold(holdId) },
        )
        holdScreen(
            navController = navController,
            onNavigateToHomeList = {
                navController.navigate(Home) {
                    popUpTo(Home) { inclusive = true }
                }
            },
        )
        holdHistoryScreen(
            navController = navController,
            onNavigateToMenu = onNavigateToMenu,
        )
    }
}
