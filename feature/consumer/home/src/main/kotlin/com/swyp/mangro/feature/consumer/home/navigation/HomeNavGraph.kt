package com.swyp.mangro.feature.consumer.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.feature.consumer.hold.navigation.navigateToHold
import com.swyp.mangro.feature.consumer.home.HomeRoute
import com.swyp.mangro.feature.consumer.store.navigation.navigateToProductDetail
import kotlinx.serialization.Serializable

@Serializable
data object Home

fun NavGraphBuilder.homeNavGraph(
    navController: NavController,
    onNavigateToMenu: (ConsumerMenu) -> Unit,
) {
    composable<Home> {
        HomeRoute(
            navigateToProductDetail = { productId ->
                navController.navigateToProductDetail(productId)
            },
            navigateToHold = { holdId -> navController.navigateToHold(holdId) },
            navigateToWishList = { onNavigateToMenu(ConsumerMenu.WISH_LIST) },
            navigateToMy = { onNavigateToMenu(ConsumerMenu.MY) },
        )
    }
}
