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
    onNavigateToHomeList: () -> Unit,
) {
    composable<HoldDestination> {
        HoldRoute(
            onNavigateToProductDetail = { navController.popBackStack() },
            onNavigateToHomeList = onNavigateToHomeList,
        )
    }
}

fun NavController.navigateToHold(holdId: String) {
    navigate(HoldDestination(holdId = holdId))
}
