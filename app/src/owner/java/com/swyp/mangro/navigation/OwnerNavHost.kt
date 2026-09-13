package com.swyp.mangro.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.swyp.mangro.R
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.feature.owner.home.navigation.OwnerHomeDestination
import com.swyp.mangro.feature.owner.home.navigation.ownerHomeNavGraph
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.navigation.OwnerProductEditorDestination
import com.swyp.mangro.feature.owner.product.navigation.ownerProductNavGraph
import com.swyp.mangro.feature.owner.product.screen.detail.OwnerProductDetailDestination
import com.swyp.mangro.feature.owner.product.screen.list.OwnerProductListDestination
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.Serializable

@Serializable
private data class OwnerProductCancellationDestination(val productIds: List<String>)

@Composable
internal fun OwnerNavHost(
    products: List<OwnerProductModel>,
    storeClosingTime: String,
    storeOpeningTime: String,
    onSaveProducts: (List<OwnerProductModel>) -> Unit,
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val pickupUnavailable = stringResource(R.string.owner_pickup_unavailable)
    NavHost(navController, startDestination = OwnerHomeDestination) {
        ownerHomeNavGraph(
            products = products.map {
                OwnerProduct(it.id, it.photos.firstOrNull().orEmpty(), it.name, it.salePrice, it.remainingQuantity, it.reservedQuantity, 0)
            }.toPersistentList(),
            navigateToProducts = {
                navController.navigate(OwnerProductListDestination) {
                    popUpTo<OwnerHomeDestination> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            navigateToProduct = { navController.navigate(OwnerProductDetailDestination(it)) },
            navigateToRegisterProduct = { navController.navigate(OwnerProductEditorDestination) },
        )

        ownerProductNavGraph(
            navController = navController,
            products = products,
            storeClosingTime = storeClosingTime,
            storeOpeningTime = storeOpeningTime,
            onSaveProducts = onSaveProducts,
            onCancelReservations = { navController.navigate(OwnerProductCancellationDestination(it)) },
            onMenuClick = { menu ->
                if (menu == OwnerMenu.HOME) {
                    navController.popBackStack<OwnerHomeDestination>(inclusive = false, saveState = true)
                }
            },
            onPickupClick = { Toast.makeText(context, pickupUnavailable, Toast.LENGTH_SHORT).show() },
            onCompletePickup = { Toast.makeText(context, pickupUnavailable, Toast.LENGTH_SHORT).show() },
        )

        composable<OwnerProductCancellationDestination> { entry ->
            val productIds = entry.toRoute<OwnerProductCancellationDestination>().productIds
            Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(stringResource(R.string.owner_product_cancellation_title))
                products.filter { it.id in productIds }.forEach {
                    Text(stringResource(R.string.owner_product_cancellation_summary, it.name, it.shortageQuantity))
                }
                Text(stringResource(R.string.owner_product_cancellation_error))
                MangroButton(stringResource(R.string.owner_product_return), { navController.popBackStack() }, MangroButtonStyle.ACTIVE)
            }
        }
    }
}
