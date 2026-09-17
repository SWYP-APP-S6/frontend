package com.swyp.mangro.feature.owner.product.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.OwnerPickupCancellationDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationRoute
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.OwnerPickupDetailDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.PickupDetailRoute

fun NavGraphBuilder.ownerPickupNavGraph(navController: NavHostController, navigateBack: () -> Unit, navigateToHome: () -> Unit) {
    val onHoldsChanged = {
        navController.getBackStackEntry(navController.graph.startDestinationId).savedStateHandle[HOLDS_CHANGED] = true
    }
    composable<OwnerPickupDetailDestination> {
        PickupDetailRoute(navigateBack = navigateBack, navigateToHome = navigateToHome, onHoldsChanged = onHoldsChanged)
    }
    composable<OwnerPickupCancellationDestination> {
        PickupCancellationRoute(navigateBack = navigateBack, onHoldsChanged = onHoldsChanged)
    }
}
