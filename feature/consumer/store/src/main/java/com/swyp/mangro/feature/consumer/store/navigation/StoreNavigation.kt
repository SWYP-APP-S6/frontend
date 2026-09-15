package com.swyp.mangro.feature.consumer.store.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.swyp.mangro.feature.consumer.store.product.ProductDetailRoute
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailRoute(val productId: String)

fun NavGraphBuilder.productDetailScreen(
    navController: NavController,
) {
    composable<ProductDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ProductDetailRoute>()

        ProductDetailRoute(
            onBackClick = { navController.popBackStack() },
            onNavigateToStoreDetail = { },
        )
    }
}

fun NavController.navigateToProductDetail(productId: String) {
    navigate(ProductDetailRoute(productId = productId))
}
