package com.swyp.mangro.feature.owner.product.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.detail.OwnerProductDetailDestination
import com.swyp.mangro.feature.owner.product.screen.detail.ProductDetailRoute
import com.swyp.mangro.feature.owner.product.screen.list.OwnerProductListDestination
import com.swyp.mangro.feature.owner.product.screen.list.ProductListFilter
import com.swyp.mangro.feature.owner.product.screen.list.ProductListRoute
import com.swyp.mangro.feature.owner.product.screen.list.ProductListViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.serialization.Serializable

@Serializable
data object OwnerProductEditorDestination

fun NavGraphBuilder.ownerProductNavGraph(
    navController: NavHostController,
    products: List<OwnerProductModel>,
    onSaveProducts: (List<OwnerProductModel>) -> Unit,
    onCancelReservations: (List<String>) -> Unit,
    onMenuClick: (OwnerMenu) -> Unit,
    onPickupClick: (String) -> Unit,
) {
    composable<OwnerProductListDestination> { entry ->
        val viewModel: ProductListViewModel = hiltViewModel()
        LaunchedEffect(entry, viewModel) {
            entry.savedStateHandle.getStateFlow<String?>(PICKUP_ENTRY_FILTER, null).filterNotNull().collect { filter ->
                viewModel.openPickups(ProductListFilter.valueOf(filter))
                entry.savedStateHandle[PICKUP_ENTRY_FILTER] = null
            }
        }
        LaunchedEffect(entry, viewModel) {
            val resultHandle = navController.getBackStackEntry(navController.graph.startDestinationId).savedStateHandle
            resultHandle.getStateFlow(HOLDS_CHANGED, false).collect { changed ->
                if (changed) {
                    viewModel.refresh()
                    resultHandle[HOLDS_CHANGED] = false
                }
            }
        }
        ProductListRoute(
            viewModel = viewModel,
            products = products,
            onMenuClick = onMenuClick,
            onPickupClick = onPickupClick,
            onSelect = { navController.navigate(OwnerProductDetailDestination(it)) },
            onCancelReservations = onCancelReservations,
        )
    }
    composable<OwnerProductDetailDestination> {
        ProductDetailRoute(
            onBack = { navController.popBackStack() },
            onCancelReservations = { onCancelReservations(listOf(it)) },
        )
    }
    composable<OwnerProductEditorDestination> {
        ProductRegistrationRoute(
            onBack = { navController.popBackStack() },
            onSave = {
                onSaveProducts(listOf(it))
                navController.popBackStack()
            },
        )
    }
}

internal const val PICKUP_ENTRY_FILTER = "pickup_entry_filter"

fun NavHostController.navigateToOwnerPickups(
    filter: ProductListFilter,
    builder: NavOptionsBuilder.() -> Unit,
) {
    navigate(OwnerProductListDestination, builder)
    getBackStackEntry<OwnerProductListDestination>().savedStateHandle[PICKUP_ENTRY_FILTER] = filter.name
}

internal const val HOLDS_CHANGED = "owner_holds_changed"
