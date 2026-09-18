package com.swyp.mangro.feature.consumer.hold.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.feature.consumer.hold.complete.PickupCompleteRoute
import com.swyp.mangro.feature.consumer.hold.detail.HoldDetailRoute
import com.swyp.mangro.feature.consumer.hold.history.HoldHistoryRoute
import com.swyp.mangro.feature.consumer.hold.hold.HoldRoute
import kotlinx.serialization.Serializable

@Serializable
data class HoldDestination(val holdId: String)

@Serializable
data object HoldHistoryDestination

@Serializable
data class HoldDetailDestination(val holdId: String)

@Serializable
data class PickupCompleteDestination(val holdId: String)

fun NavGraphBuilder.holdScreen(
    navController: NavController,
    onNavigateToHomeList: () -> Unit,
) {
    composable<HoldDestination> {
        HoldRoute(
            onNavigateToProductDetail = { navController.popBackStack() },
            onNavigateToHomeList = onNavigateToHomeList,
            onNavigateToPickupComplete = { holdId -> navController.navigateToPickupComplete(holdId) },
        )
    }
}

fun NavGraphBuilder.holdHistoryScreen(
    navController: NavController,
    onNavigateToMenu: (ConsumerMenu) -> Unit,
) {
    composable<HoldHistoryDestination> {
        HoldHistoryRoute(
            onNavigateToHoldDetail = { holdId -> navController.navigateToHoldDetail(holdId) },
            onNavigateToMenu = onNavigateToMenu,
        )
    }
}

fun NavGraphBuilder.holdDetailScreen(
    navController: NavController,
) {
    composable<HoldDetailDestination> {
        HoldDetailRoute(
            onBackClick = { navController.popBackStack() },
            onNavigateToHoldHistory = {
                navController.popBackStack()
            },
        )
    }
}

fun NavGraphBuilder.pickupCompleteScreen(
    navController: NavController,
    onNavigateToRecipeDetail: (Long) -> Unit,
    onNavigateToMenu: (ConsumerMenu) -> Unit,
) {
    composable<PickupCompleteDestination> {
        PickupCompleteRoute(
            onBackClick = { navController.popBackStack() },
            onNavigateToRecipeDetail = onNavigateToRecipeDetail,
            onNavigateToMenu = onNavigateToMenu,
        )
    }
}

fun NavController.navigateToHold(holdId: String) {
    navigate(HoldDestination(holdId = holdId))
}

fun NavController.navigateToHoldHistory() {
    navigate(HoldHistoryDestination)
}

fun NavController.navigateToHoldDetail(holdId: String) {
    navigate(HoldDetailDestination(holdId = holdId))
}

fun NavController.navigateToPickupComplete(holdId: String) {
    navigate(PickupCompleteDestination(holdId = holdId)) {
        popUpTo(HoldDestination(holdId = holdId)) { inclusive = true }
    }
}
