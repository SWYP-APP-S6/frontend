package com.swyp.mangro.feature.owner.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.feature.owner.home.screen.OwnerHomePickupState
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeScreenRoute
import kotlinx.collections.immutable.PersistentList
import kotlinx.serialization.Serializable

@Serializable
data object OwnerHomeDestination

fun NavGraphBuilder.ownerHomeNavGraph(
    pickups: OwnerHomePickupState,
    navigateToStore: () -> Unit,
    navigateToRegisterStore: () -> Unit,
    navigateToPickups: () -> Unit,
    navigateToCompletedPickups: () -> Unit,
    navigateToExpiredPickups: () -> Unit,
    navigateToCancellations: () -> Unit,
    navigateToPickup: (String) -> Unit,
    onCompletePickup: (String) -> Boolean,
    products: PersistentList<OwnerProduct>,
    navigateToProducts: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToProduct: (String) -> Unit,
    navigateToRegisterProduct: () -> Unit,
) {
    composable<OwnerHomeDestination> {
        OwnerHomeScreenRoute(
            pickups = pickups,
            navigateToStore = navigateToStore,
            navigateToRegisterStore = navigateToRegisterStore,
            navigateToPickups = navigateToPickups,
            navigateToCompletedPickups = navigateToCompletedPickups,
            navigateToExpiredPickups = navigateToExpiredPickups,
            navigateToCancellations = navigateToCancellations,
            navigateToPickup = navigateToPickup,
            onCompletePickup = onCompletePickup,
            products = products,
            navigateToProducts = navigateToProducts,
            navigateToSettings = navigateToSettings,
            navigateToProduct = navigateToProduct,
            navigateToRegisterProduct = navigateToRegisterProduct,
        )
    }
}
