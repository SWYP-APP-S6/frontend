package com.swyp.mangro

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.swyp.mangro.core.utils.HideNavigationBarWhileVisible
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.feature.auth.navigation.Login
import com.swyp.mangro.feature.auth.navigation.authNavGraph
import com.swyp.mangro.feature.owner.onboarding.navigation.OwnerOnboardingNavigation
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.splash.navigation.Splash
import com.swyp.mangro.feature.splash.navigation.splashNavGraph
import com.swyp.mangro.navigation.OwnerNavHost
import com.swyp.mangro.notification.OwnerNotificationPermission
import com.swyp.mangro.notification.OwnerNotificationReadWorker
import com.swyp.mangro.notification.OwnerStockReconfirmationRequests
import com.swyp.mangro.notification.model.OwnerNotificationOpen
import com.swyp.mangro.theme.MangroTheme
import kotlinx.serialization.Serializable

/** Local UI host until the catalog repository is connected. */
@Composable
internal fun MainScreen(notificationIntent: Intent? = null) {
    val context = LocalContext.current
    val opened = OwnerNotificationOpen.from(notificationIntent?.getStringExtra("type"), notificationIntent?.getStringExtra("notificationId"))
    val openKey = opened?.let { "${it.type}:${it.notificationId}" }
    var consumedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val pendingOpen = openKey?.takeIf { it != consumedKey }

    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    HideNavigationBarWhileVisible(hidden = currentRoute == Login::class.qualifiedName || currentRoute == Splash::class.qualifiedName)

    NavHost(navController, startDestination = Splash) {
        splashNavGraph(
            navigateToLogin = { navController.navigate(Login) { popUpTo<Splash> { inclusive = true } } },
            navigateToHome = { navController.navigate(OwnerMain) { popUpTo<Splash> { inclusive = true } } },
        )
        authNavGraph(
            navController = navController,
            navigateToHome = {
                navController.navigate(OwnerMain) {
                    popUpTo<Login> { inclusive = true }
                }
            },
            onOwnerOnboarding = { consents ->
                navController.navigate(OwnerOnboarding(consents.service, consents.privacy, consents.location, consents.thirdParty, consents.marketing)) {
                    launchSingleTop = true
                }
            },
        )
        composable<OwnerOnboarding> { entry ->
            val route = entry.toRoute<OwnerOnboarding>()
            OwnerOnboardingNavigation(
                navController = rememberNavController(),
                consents = SignupConsents(route.service, route.privacy, route.location, route.thirdParty, route.marketing),
                onLoginRequired = { navController.popBackStack<Login>(inclusive = false) },
                onComplete = {
                    navController.navigate(OwnerMain) {
                        popUpTo<Login> { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable<OwnerMain> {
            OwnerMainContent(
                notificationKey = pendingOpen,
                onNotificationOpened = {
                    opened?.notificationId?.let { OwnerNotificationReadWorker.enqueue(context, it) }
                    consumedKey = openKey
                },
                onLogout = {
                    OwnerStockReconfirmationRequests.clear()
                    navController.navigate(Login) {
                        popUpTo<OwnerMain> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

@Serializable
private data object OwnerMain

@Serializable
private data class OwnerOnboarding(val service: Boolean, val privacy: Boolean, val location: Boolean, val thirdParty: Boolean, val marketing: Boolean)

@Composable
internal fun OwnerMainContent(notificationKey: String? = null, onNotificationOpened: () -> Unit = {}, onLogout: () -> Unit = {}) {
    OwnerNotificationPermission()
    var products by rememberSaveable { mutableStateOf(emptyList<OwnerProductModel>()) }
    MangroTheme {
        OwnerNavHost(
            notificationKey = notificationKey,
            onNotificationOpened = onNotificationOpened,
            onLogout = onLogout,
            products = products,
            onSaveProducts = { changed ->
                val ids = changed.map { it.id }.toSet()
                products = products.filterNot { it.id in ids } + changed
            },
        )
    }
}
