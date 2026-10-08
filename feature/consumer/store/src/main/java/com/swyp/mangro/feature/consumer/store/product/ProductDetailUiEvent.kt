package com.swyp.mangro.feature.consumer.store.product

import androidx.annotation.StringRes
import com.swyp.mangro.core.model.store.StoreInfo

sealed interface ProductDetailUiEvent {
    data object NavigateToStoreDetail : ProductDetailUiEvent
    data class WishConfirmed(val holdId: String) : ProductDetailUiEvent
    data class OpenMapDirections(val storeInfo: StoreInfo) : ProductDetailUiEvent
    data object ShowLoginRequiredDialog : ProductDetailUiEvent
    data class NavigateToRecipeDetail(val recipeId: Long) : ProductDetailUiEvent
    data class ShowToast(@StringRes val messageRes: Int) : ProductDetailUiEvent
}
