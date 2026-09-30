package com.swyp.mangro

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.swyp.mangro.core.designsystem.component.appbar.FloatingBottomAppBarOverlayHeight
import com.swyp.mangro.core.designsystem.component.appbar.LocalFloatingBottomAppBarOverlayHeight
import com.swyp.mangro.core.designsystem.component.appbar.OwnerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.core.utils.HideNavigationBarWhileVisible
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.feature.auth.navigation.Login
import com.swyp.mangro.feature.auth.navigation.authNavGraph
import com.swyp.mangro.feature.owner.home.navigation.OwnerHomeDestination
import com.swyp.mangro.feature.owner.onboarding.navigation.OwnerOnboardingNavigation
import com.swyp.mangro.feature.owner.product.screen.list.OwnerProductListDestination
import com.swyp.mangro.feature.owner.setting.navigation.OwnerSettingDestination
import com.swyp.mangro.feature.splash.navigation.Splash
import com.swyp.mangro.feature.splash.navigation.splashNavGraph
import com.swyp.mangro.navigation.OwnerNavHost
import com.swyp.mangro.notification.OwnerNotificationPermission
import com.swyp.mangro.notification.OwnerNotificationReadWorker
import com.swyp.mangro.notification.OwnerStockReconfirmationRequests
import com.swyp.mangro.notification.model.OwnerNotificationOpen
import com.swyp.mangro.theme.MangroTheme
import kotlinx.serialization.Serializable

@Serializable
private data object OwnerMain

@Serializable
private data class OwnerOnboarding(
    val service: Boolean,
    val privacy: Boolean,
    val location: Boolean,
    val thirdParty: Boolean,
    val marketing: Boolean,
)

/** Local UI host until the catalog repository is connected. */
@Composable
internal fun MainScreen(notificationIntent: Intent? = null) {
    val context = LocalContext.current
    val opened = OwnerNotificationOpen.from(
        notificationIntent?.getStringExtra("type"),
        notificationIntent?.getStringExtra("notificationId"),
        notificationIntent?.getStringExtra("deepLink") ?: notificationIntent?.dataString,
    )
    val openKey = opened?.key
    var consumedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val pendingOpen = opened?.takeIf { openKey != consumedKey }

    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    HideNavigationBarWhileVisible(hidden = currentRoute == Login::class.qualifiedName || currentRoute == Splash::class.qualifiedName)

    NavHost(
        navController = navController,
        startDestination = Splash,
    ) {
        splashNavGraph(
            navigateToLogin = {
                navController.navigate(Login) {
                    popUpTo<Splash> { inclusive = true }
                }
            },
            navigateToHome = {
                navController.navigate(OwnerMain) {
                    popUpTo<Splash> { inclusive = true }
                }
            },
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
                notificationOpen = pendingOpen,
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun OwnerMainContent(
    notificationOpen: OwnerNotificationOpen? = null,
    onNotificationOpened: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    OwnerNotificationPermission()

    MangroTheme {
        val activity = LocalActivity.current
        val view = LocalView.current

        DisposableEffect(activity, view) {
            val window = activity?.window
            val previousContrast = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window?.isNavigationBarContrastEnforced else null
            val previousNavigationBarColor = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) window?.navigationBarColor else null
            val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
            val previousLightIcons = insetsController?.isAppearanceLightNavigationBars

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window?.isNavigationBarContrastEnforced = false
            } else {
                window?.navigationBarColor = Color.TRANSPARENT
            }
            insetsController?.isAppearanceLightNavigationBars = true

            onDispose {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && previousContrast != null) {
                    window?.isNavigationBarContrastEnforced = previousContrast
                } else if (previousNavigationBarColor != null) {
                    window?.navigationBarColor = previousNavigationBarColor
                }
                if (previousLightIcons != null) insetsController.isAppearanceLightNavigationBars = previousLightIcons
            }
        }

        val navController = rememberNavController()

        val destination = navController.currentBackStackEntryAsState().value?.destination
        val currentDestination = when {
            destination?.hasRoute<OwnerHomeDestination>() == true -> OwnerMenu.HOME
            destination?.hasRoute<OwnerProductListDestination>() == true -> OwnerMenu.STORE
            destination?.hasRoute<OwnerSettingDestination>() == true -> OwnerMenu.SETTINGS
            else -> null
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                currentDestination?.let { menu ->
                    OwnerBottomAppBar(
                        currentMenu = menu,
                        onMenuClick = { selected ->
                            if (selected == menu) return@OwnerBottomAppBar

                            when (selected) {
                                OwnerMenu.HOME -> {
                                    navController.navigate(OwnerHomeDestination) {
                                        popUpTo<OwnerHomeDestination> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }

                                OwnerMenu.STORE -> {
                                    navController.navigate(OwnerProductListDestination) {
                                        popUpTo<OwnerHomeDestination> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }

                                OwnerMenu.SETTINGS -> {
                                    navController.navigate(OwnerSettingDestination) {
                                        popUpTo<OwnerHomeDestination> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        },
                    )
                }
            },
        ) { _ ->
            // Draw the NavHost behind the capsule; tab screens reserve space for their final items.
            CompositionLocalProvider(
                LocalFloatingBottomAppBarOverlayHeight provides if (currentDestination != null) FloatingBottomAppBarOverlayHeight else 0.dp,
            ) {
                OwnerNavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize(),
                    notificationOpen = notificationOpen,
                    onNotificationOpened = onNotificationOpened,
                    onLogout = onLogout,
                )
            }
        }
    }
}
