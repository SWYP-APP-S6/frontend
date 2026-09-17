package com.swyp.mangro.feature.owner.product.screen.detail

sealed interface ProductDetailEvent {
    data object NavigateBack : ProductDetailEvent
    data class NavigateToCancellations(val productId: String) : ProductDetailEvent
}
