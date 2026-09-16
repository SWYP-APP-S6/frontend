package com.swyp.mangro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.feature.owner.home.navigation.OwnerHomeDestination
import com.swyp.mangro.feature.owner.home.navigation.ownerHomeNavGraph
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.navigation.OwnerProductEditorDestination
import com.swyp.mangro.feature.owner.product.navigation.ownerPickupNavGraph
import com.swyp.mangro.feature.owner.product.navigation.ownerProductNavGraph
import com.swyp.mangro.feature.owner.product.screen.detail.OwnerProductDetailDestination
import com.swyp.mangro.feature.owner.product.screen.list.OwnerProductListDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.OwnerPickupCancellationDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.OwnerPickupDetailDestination
import com.swyp.mangro.feature.owner.setting.navigation.OwnerPolicyDestination
import com.swyp.mangro.feature.owner.setting.navigation.OwnerSettingDestination
import com.swyp.mangro.feature.owner.setting.navigation.ownerSettingNavGraph

@Composable
internal fun OwnerNavHost(
    products: List<OwnerProductModel>,
    onSaveProducts: (List<OwnerProductModel>) -> Unit,
    notificationKey: String? = null,
    onNotificationOpened: () -> Unit = {},
) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = OwnerHomeDestination) {
        ownerHomeNavGraph(
            navigateToProducts = {
                navController.navigate(OwnerProductListDestination) {
                    popUpTo<OwnerHomeDestination> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            navigateToSettings = {
                navController.navigate(OwnerSettingDestination) {
                    popUpTo<OwnerHomeDestination> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            navigateToProduct = { navController.navigate(OwnerProductDetailDestination(it)) },
            navigateToRegisterProduct = { navController.navigate(OwnerProductEditorDestination) },
        )

        ownerProductNavGraph(
            navController = navController,
            products = products,
            onSaveProducts = onSaveProducts,
            onCancelReservations = { navController.navigate(OwnerPickupCancellationDestination) },
            onMenuClick = { menu ->
                when (menu) {
                    OwnerMenu.HOME -> navController.popBackStack<OwnerHomeDestination>(inclusive = false, saveState = true)
                    OwnerMenu.STORE -> Unit
                    OwnerMenu.SETTINGS -> navController.navigate(OwnerSettingDestination) {
                        popUpTo<OwnerHomeDestination> { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onPickupClick = { navController.navigate(OwnerPickupDetailDestination(it)) },
        )

        ownerSettingNavGraph(
            navigateToHome = { navController.popBackStack<OwnerHomeDestination>(inclusive = false, saveState = true) },
            navigateToProducts = {
                navController.navigate(OwnerProductListDestination) {
                    popUpTo<OwnerHomeDestination> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            navigateToPolicy = { navController.navigate(OwnerPolicyDestination(it)) { launchSingleTop = true } },
            navigateBack = { navController.popBackStack() },
        )

        ownerPickupNavGraph(
            navigateBack = { navController.popBackStack() },
            navigateToHome = { navController.popBackStack<OwnerHomeDestination>(inclusive = false) },
        )
    }
    LaunchedEffect(notificationKey) {
        if (notificationKey != null) {
            navController.navigate(OwnerHomeDestination) {
                popUpTo<OwnerHomeDestination> { inclusive = true }
                launchSingleTop = true
            }
            onNotificationOpened()
        }
    }
}
