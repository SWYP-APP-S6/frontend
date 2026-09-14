package com.swyp.mangro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
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
import kotlinx.collections.immutable.toPersistentList

@Composable
internal fun OwnerNavHost(
    products: List<OwnerProductModel>,
    storeClosingTime: String,
    storeOpeningTime: String,
    onSaveProducts: (List<OwnerProductModel>) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = OwnerHomeDestination) {
        ownerHomeNavGraph(
            products = products.map {
                OwnerProduct(it.id, it.photos.firstOrNull().orEmpty(), it.name, it.salePrice, it.remainingQuantity, it.reservedQuantity, 0)
            }.toPersistentList(),
            navigateToProducts = {
                navController.navigate(OwnerProductListDestination) {
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
            storeClosingTime = storeClosingTime,
            storeOpeningTime = storeOpeningTime,
            onSaveProducts = onSaveProducts,
            onCancelReservations = { navController.navigate(OwnerPickupCancellationDestination) },
            onMenuClick = { menu ->
                if (menu == OwnerMenu.HOME) {
                    navController.popBackStack<OwnerHomeDestination>(inclusive = false, saveState = true)
                }
            },
            onPickupClick = { navController.navigate(OwnerPickupDetailDestination(it)) },
        )

        ownerPickupNavGraph(
            navigateBack = { navController.popBackStack() },
            navigateToHome = { navController.popBackStack<OwnerHomeDestination>(inclusive = false) },
        )
    }
}
