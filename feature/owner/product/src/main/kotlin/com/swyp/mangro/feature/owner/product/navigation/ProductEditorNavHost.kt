package com.swyp.mangro.feature.owner.product.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.model.ProductDraftModel
import com.swyp.mangro.feature.owner.product.screen.editor.basic.ProductBasicInfoDestination
import com.swyp.mangro.feature.owner.product.screen.editor.basic.ProductBasicInfoRoute
import com.swyp.mangro.feature.owner.product.screen.editor.pickup.ProductPickupInfoDestination
import com.swyp.mangro.feature.owner.product.screen.editor.pickup.ProductPickupInfoRoute
import com.swyp.mangro.feature.owner.product.screen.editor.price.ProductPriceDestination
import com.swyp.mangro.feature.owner.product.screen.editor.price.ProductPriceRoute

@Composable
internal fun ProductEditorNavHost(
    storeClosingTime: String,
    storeOpeningTime: String,
    onBack: () -> Unit,
    onSave: (OwnerProductModel) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = ProductBasicInfoDestination) {
        composable<ProductBasicInfoDestination> { entry ->
            ProductBasicInfoRoute(
                onNext = {
                    entry.savedStateHandle[DRAFT] = it
                    navController.navigate(ProductPriceDestination) {
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                onExit = onBack,
            )
        }
        composable<ProductPriceDestination> { entry ->
            val basicEntry = remember(entry) { navController.getBackStackEntry<ProductBasicInfoDestination>() }
            val draft by basicEntry.savedStateHandle.getStateFlow<ProductDraftModel?>(DRAFT, null).collectAsStateWithLifecycle()
            ProductPriceRoute(
                draft = draft,
                onNext = {
                    entry.savedStateHandle[DRAFT] = it
                    navController.navigate(ProductPickupInfoDestination) {
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack<ProductPriceDestination>(inclusive = true, saveState = true) },
            )
        }
        composable<ProductPickupInfoDestination> { entry ->
            val priceEntry = remember(entry) { navController.getBackStackEntry<ProductPriceDestination>() }
            val draft by priceEntry.savedStateHandle.getStateFlow<ProductDraftModel?>(DRAFT, null).collectAsStateWithLifecycle()
            ProductPickupInfoRoute(
                draft = draft,
                storeClosingTime = storeClosingTime,
                storeOpeningTime = storeOpeningTime,
                onBack = { navController.popBackStack<ProductPickupInfoDestination>(inclusive = true, saveState = true) },
                onEditBasicInfo = {
                    navController.popBackStack<ProductPickupInfoDestination>(inclusive = true, saveState = true)
                    navController.popBackStack<ProductPriceDestination>(inclusive = true, saveState = true)
                },
                onSave = onSave,
            )
        }
    }
}

private const val DRAFT = "productDraft"
