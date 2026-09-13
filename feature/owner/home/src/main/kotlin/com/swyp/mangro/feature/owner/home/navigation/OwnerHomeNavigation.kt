package com.swyp.mangro.feature.owner.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeScreenRoute
import kotlinx.collections.immutable.PersistentList
import kotlinx.serialization.Serializable

@Serializable
data object OwnerHomeDestination

fun NavGraphBuilder.ownerHomeNavGraph(
    products: PersistentList<OwnerProduct>,
    navigateToProducts: () -> Unit,
    navigateToProduct: (String) -> Unit,
    navigateToRegisterProduct: () -> Unit,
) {
    composable<OwnerHomeDestination> {
        OwnerHomeScreenRoute(
            products = products,
            navigateToProducts = navigateToProducts,
            navigateToProduct = navigateToProduct,
            navigateToRegisterProduct = navigateToRegisterProduct,
        )
    }
}
