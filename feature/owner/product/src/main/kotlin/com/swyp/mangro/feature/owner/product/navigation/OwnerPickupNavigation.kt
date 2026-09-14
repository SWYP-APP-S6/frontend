package com.swyp.mangro.feature.owner.product.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.OwnerPickupCancellationDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationRoute
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.OwnerPickupDetailDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.PickupDetailRoute

fun NavGraphBuilder.ownerPickupNavGraph(navigateBack: () -> Unit, navigateToHome: () -> Unit) {
    composable<OwnerPickupDetailDestination> {
        PickupDetailRoute(navigateBack = navigateBack, navigateToHome = navigateToHome)
    }
    composable<OwnerPickupCancellationDestination> {
        PickupCancellationRoute(navigateBack = navigateBack)
    }
}
