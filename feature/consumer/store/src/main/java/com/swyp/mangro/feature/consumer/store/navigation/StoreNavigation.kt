package com.swyp.mangro.feature.consumer.store.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.consumer.store.product.ProductDetailRoute
import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailRoute(val productId: String)

fun NavGraphBuilder.productDetailScreen(
    navController: NavController,
    onNavigateToHold: (String) -> Unit,
) {
    composable<ProductDetailRoute> {
        ProductDetailRoute(
            onBackClick = { navController.popBackStack() },
            onNavigateToStoreDetail = { },
            onNavigateToHold = onNavigateToHold,
        )
    }
}

fun NavController.navigateToProductDetail(productId: String) {
    navigate(ProductDetailRoute(productId = productId))
}
