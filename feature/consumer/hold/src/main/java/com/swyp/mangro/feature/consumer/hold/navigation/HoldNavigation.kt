package com.swyp.mangro.feature.consumer.hold.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.consumer.hold.hold.HoldRoute
import kotlinx.serialization.Serializable

@Serializable
data class HoldDestination(val holdId: String)

fun NavGraphBuilder.holdScreen(
    navController: NavController,
) {
    composable<HoldDestination> {
        HoldRoute(
            onNavigateToProductDetail = { navController.popBackStack() },
        )
    }
}

fun NavController.navigateToHold(holdId: String) {
    navigate(HoldDestination(holdId = holdId))
}
