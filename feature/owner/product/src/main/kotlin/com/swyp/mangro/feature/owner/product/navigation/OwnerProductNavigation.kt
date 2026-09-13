package com.swyp.mangro.feature.owner.product.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.OwnerProductScaffold
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.detail.ProductDetailRoute
import com.swyp.mangro.feature.owner.product.screen.editor.ProductEditorScreen
import com.swyp.mangro.feature.owner.product.screen.list.ProductListScreen
import com.swyp.mangro.feature.owner.product.screen.stock.ProductStockScreen
import kotlinx.serialization.Serializable

@Serializable
data object OwnerProductListDestination

@Serializable
data class OwnerProductDetailDestination(val productId: String)

@Serializable
data class OwnerProductEditorDestination(val productId: String? = null)

@Serializable
data object OwnerProductStockDestination

fun NavGraphBuilder.ownerProductNavGraph(
    navController: NavHostController,
    products: List<OwnerProductModel>,
    storeClosingTime: String,
    onSaveProducts: (List<OwnerProductModel>) -> Unit,
    onCancelReservations: (List<String>) -> Unit,
) {
    composable<OwnerProductListDestination> {
        ProductListScreen(
            products = products,
            onBack = { navController.popBackStack() },
            onAdd = { navController.navigate(OwnerProductEditorDestination()) },
            onSelect = { navController.navigate(OwnerProductDetailDestination(it)) },
            onStock = { navController.navigate(OwnerProductStockDestination) },
            onCancelReservations = onCancelReservations,
        )
    }
    composable<OwnerProductDetailDestination> { entry ->
        val productId = entry.toRoute<OwnerProductDetailDestination>().productId
        val product = products.find { it.id == productId }
        if (product == null) {
            MissingProductScreen { navController.popBackStack() }
        } else {
            ProductDetailRoute(
                product = product,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(OwnerProductEditorDestination(productId)) },
                onSave = { onSaveProducts(listOf(it)) },
                onCancelReservations = { onCancelReservations(listOf(productId)) },
            )
        }
    }
    composable<OwnerProductEditorDestination> { entry ->
        val productId = entry.toRoute<OwnerProductEditorDestination>().productId
        val product = products.find { it.id == productId }
        if (productId != null && product == null) {
            MissingProductScreen { navController.popBackStack() }
        } else {
            ProductEditorScreen(
                product = product,
                storeClosingTime = storeClosingTime,
                onBack = { navController.popBackStack() },
                onSave = {
                    onSaveProducts(listOf(it))
                    navController.popBackStack()
                },
            )
        }
    }
    composable<OwnerProductStockDestination> {
        ProductStockScreen(
            products = products,
            onBack = { navController.popBackStack() },
            onSave = onSaveProducts,
            onCancelReservations = onCancelReservations,
        )
    }
}

@Composable
private fun MissingProductScreen(onBack: () -> Unit) {
    OwnerProductScaffold(stringResource(R.string.owner_product_management_title), onBack) {
        Text(stringResource(R.string.owner_product_product_missing), style = MangroTheme.typography.caption.captionS, color = MangroTheme.colors.textSubtitle)
    }
}
