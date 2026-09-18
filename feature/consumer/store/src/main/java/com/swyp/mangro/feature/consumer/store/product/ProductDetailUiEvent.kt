package com.swyp.mangro.feature.consumer.store.product

sealed interface ProductDetailUiEvent {
    data object NavigateToStoreDetail : ProductDetailUiEvent
    data class WishConfirmed(val holdId: String) : ProductDetailUiEvent
}
