package com.swyp.mangro.feature.owner.product.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.detail.OwnerProductDetailDestination
import com.swyp.mangro.feature.owner.product.screen.detail.ProductDetailRoute
import com.swyp.mangro.feature.owner.product.screen.list.OwnerProductListDestination
import com.swyp.mangro.feature.owner.product.screen.list.ProductListRoute
import kotlinx.serialization.Serializable

@Serializable
data object OwnerProductEditorDestination

fun NavGraphBuilder.ownerProductNavGraph(
    navController: NavHostController,
    products: List<OwnerProductModel>,
    onSaveProducts: (List<OwnerProductModel>) -> Unit,
    onCancelReservations: (List<String>) -> Unit,
    onMenuClick: (OwnerMenu) -> Unit,
    onPickupClick: (String) -> Unit,
) {
    composable<OwnerProductListDestination> {
        ProductListRoute(
            products = products,
            onMenuClick = onMenuClick,
            onPickupClick = onPickupClick,
            onSelect = { navController.navigate(OwnerProductDetailDestination(it)) },
            onCancelReservations = onCancelReservations,
        )
    }
    composable<OwnerProductDetailDestination> {
        ProductDetailRoute(
            products = products,
            onBack = { navController.popBackStack() },
            onSave = { onSaveProducts(listOf(it)) },
            onCancelReservations = { onCancelReservations(listOf(it)) },
        )
    }
    composable<OwnerProductEditorDestination> {
        ProductRegistrationRoute(
            onBack = { navController.popBackStack() },
            onSave = {
                onSaveProducts(listOf(it))
                navController.popBackStack()
            },
        )
    }
}
