package com.swyp.mangro.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.utils.HideNavigationBarWhileVisible
import com.swyp.mangro.feature.auth.navigation.Login
import com.swyp.mangro.feature.auth.navigation.authNavGraph
import com.swyp.mangro.feature.consumer.hold.navigation.HoldHistoryDestination
import com.swyp.mangro.feature.consumer.hold.navigation.holdDetailScreen
import com.swyp.mangro.feature.consumer.hold.navigation.holdHistoryScreen
import com.swyp.mangro.feature.consumer.hold.navigation.holdScreen
import com.swyp.mangro.feature.consumer.hold.navigation.navigateToHold
import com.swyp.mangro.feature.consumer.hold.navigation.pickupCompleteScreen
import com.swyp.mangro.feature.consumer.home.navigation.Home
import com.swyp.mangro.feature.consumer.home.navigation.homeNavGraph
import com.swyp.mangro.feature.consumer.recipe.navigation.navigateToRecipeDetail
import com.swyp.mangro.feature.consumer.recipe.navigation.recipeDetailScreen
import com.swyp.mangro.feature.consumer.store.navigation.navigateToProductDetail
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
        val destination = when (menu) {
            ConsumerMenu.HOME -> Home
            ConsumerMenu.WISH_LIST -> HoldHistoryDestination
            ConsumerMenu.MY -> null
        }

        val isSameDestination = currentRoute == destination?.let { it::class.qualifiedName }

        if (destination != null && !isSameDestination) {
            navController.navigate(destination) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Splash,
        enterTransition = { fadeIn(animationSpec = tween(150)) },
        exitTransition = { fadeOut(animationSpec = tween(150)) },
        popEnterTransition = { fadeIn(animationSpec = tween(150)) },
        popExitTransition = { fadeOut(animationSpec = tween(150)) },
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
        holdDetailScreen(
            navController = navController,
        )
        pickupCompleteScreen(
            navController = navController,
            onNavigateToRecipeDetail = { recipeId ->
                navController.navigateToRecipeDetail(recipeId)
            },
            onNavigateToMenu = onNavigateToMenu,
        )
        recipeDetailScreen(
            navController = navController,
            onNavigateToProductDetail = { productId ->
                navController.navigateToProductDetail(productId)
            },
        )
    }
}
