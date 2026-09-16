package com.swyp.mangro.feature.consumer.hold.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.feature.consumer.hold.history.HoldHistoryRoute
import com.swyp.mangro.feature.consumer.hold.hold.HoldRoute
import kotlinx.serialization.Serializable

@Serializable
data class HoldDestination(val holdId: String)

@Serializable
data object HoldHistoryDestination

fun NavGraphBuilder.holdScreen(
    navController: NavController,
    onNavigateToHomeList: () -> Unit,
) {
    composable<HoldDestination> {
        HoldRoute(
            onNavigateToProductDetail = { navController.popBackStack() },
            onNavigateToHomeList = onNavigateToHomeList,
        )
    }
}

fun NavGraphBuilder.holdHistoryScreen(
    navController: NavController,
    onNavigateToMenu: (ConsumerMenu) -> Unit,
) {
    composable<HoldHistoryDestination> {
        HoldHistoryRoute(
            onNavigateToHoldDetail = { holdId -> navController.navigateToHold(holdId) },
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
