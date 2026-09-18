package com.swyp.mangro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.feature.owner.home.navigation.OwnerHomeDestination
import com.swyp.mangro.feature.owner.home.navigation.ownerHomeNavGraph
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.navigation.OwnerProductEditorDestination
import com.swyp.mangro.feature.owner.product.navigation.navigateToOwnerPickups
import com.swyp.mangro.feature.owner.product.navigation.ownerPickupNavGraph
import com.swyp.mangro.feature.owner.product.navigation.ownerProductNavGraph
import com.swyp.mangro.feature.owner.product.screen.detail.OwnerProductDetailDestination
import com.swyp.mangro.feature.owner.product.screen.list.OwnerProductListDestination
import com.swyp.mangro.feature.owner.product.screen.list.ProductListFilter
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.OwnerPickupCancellationDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.OwnerPickupDetailDestination
import com.swyp.mangro.feature.owner.product.screen.reconfirmation.StockReconfirmationHost
import com.swyp.mangro.feature.owner.setting.navigation.OwnerPolicyDestination
import com.swyp.mangro.feature.owner.setting.navigation.OwnerSettingDestination
import com.swyp.mangro.feature.owner.setting.navigation.ownerSettingNavGraph
import com.swyp.mangro.notification.OwnerStockReconfirmationRequests
import com.swyp.mangro.notification.model.OwnerNotificationOpen
import com.swyp.mangro.notification.model.OwnerNotificationType
import kotlinx.coroutines.flow.filterNotNull

@Composable
internal fun OwnerNavHost(
    notificationOpen: OwnerNotificationOpen? = null,
    products: List<OwnerProductModel>,
    onSaveProducts: (List<OwnerProductModel>) -> Unit,
    onNotificationOpened: () -> Unit = {},
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    var stockRequestKey by rememberSaveable { mutableStateOf<String?>(null) }
    var stockProductId by rememberSaveable { mutableStateOf<Long?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            OwnerStockReconfirmationRequests.pending.filterNotNull().collect { request ->
                stockRequestKey = request.key
                stockProductId = request.productId
                OwnerStockReconfirmationRequests.consume(request)
            }
        }
    }
    NavHost(navController, startDestination = OwnerHomeDestination) {
        ownerHomeNavGraph(
            navigateToProducts = {
                navController.navigate(OwnerProductListDestination) {
                    popUpTo<OwnerHomeDestination> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            navigateToPickups = { completedOnly ->
                navController.navigateToOwnerPickups(
                    filter = if (completedOnly) ProductListFilter.COMPLETED else ProductListFilter.ALL,
                ) {
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
            navigateToPickup = { navController.navigate(OwnerPickupDetailDestination(it)) },
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
            navigateToLogin = onLogout,
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
            navController = navController,
            navigateBack = { navController.popBackStack() },
            navigateToHome = { navController.popBackStack<OwnerHomeDestination>(inclusive = false) },
        )
    }
    LaunchedEffect(notificationOpen) {
        if (notificationOpen != null) {
            when (notificationOpen.type) {
                OwnerNotificationType.STOCK_RECONFIRM_REQUEST -> notificationOpen.productId?.let { productId ->
                    stockRequestKey = notificationOpen.key
                    stockProductId = productId
                }

                else -> navController.navigateToOwnerPickups(
                    if (notificationOpen.type == OwnerNotificationType.HOLD_EXPIRED) ProductListFilter.EXPIRED else ProductListFilter.ALL,
                ) {
                    popUpTo<OwnerHomeDestination> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            onNotificationOpened()
        }
    }
    StockReconfirmationHost(
        requestKey = stockRequestKey,
        productId = stockProductId,
        onEditProduct = { navController.navigate(OwnerProductDetailDestination(it)) },
    )
}
