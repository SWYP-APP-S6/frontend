package com.swyp.mangro.feature.consumer.store.product

import com.swyp.mangro.core.model.store.StoreInfo

sealed interface ProductDetailUiEvent {
    data object NavigateToStoreDetail : ProductDetailUiEvent
    data class WishConfirmed(val holdId: String) : ProductDetailUiEvent
    data class OpenMapDirections(val storeInfo: StoreInfo) : ProductDetailUiEvent
    data object ShowLoginRequiredDialog : ProductDetailUiEvent
}
