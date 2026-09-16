package com.swyp.mangro.feature.consumer.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.consumer.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object Home

fun NavGraphBuilder.homeNavGraph(
    navController: NavController,
) {
    composable<Home> {
        HomeRoute(
            navigateToLocationSelector = { },
            navigateToProductDetail = { },
            navigateToWishList = { },
            navigateToMy = { },
        )
    }
}
