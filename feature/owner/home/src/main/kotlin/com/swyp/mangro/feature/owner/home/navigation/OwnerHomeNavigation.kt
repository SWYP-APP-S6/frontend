package com.swyp.mangro.feature.owner.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeScreenRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.Serializable

@Serializable
data object OwnerHomeDestination

fun NavGraphBuilder.ownerHomeNavGraph(
    navigateToProducts: () -> Unit,
    navigateToPickups: (completedOnly: Boolean) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToProduct: (String) -> Unit,
    navigateToPickup: (String) -> Unit,
    navigateToRegisterProduct: () -> Unit,
    refreshRequests: Flow<Unit> = emptyFlow(),
) {
    composable<OwnerHomeDestination> {
        OwnerHomeScreenRoute(
            refreshRequests = refreshRequests,
            navigateToProducts = navigateToProducts,
            navigateToPickups = navigateToPickups,
            navigateToSettings = navigateToSettings,
            navigateToProduct = navigateToProduct,
            navigateToPickup = navigateToPickup,
            navigateToRegisterProduct = navigateToRegisterProduct,
        )
    }
}
