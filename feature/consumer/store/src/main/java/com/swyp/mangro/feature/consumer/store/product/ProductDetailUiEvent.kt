package com.swyp.mangro.feature.consumer.store.product

sealed interface ProductDetailUiEvent {
    data object NavigateToStoreDetail : ProductDetailUiEvent
    data object WishConfirmed : ProductDetailUiEvent
}
