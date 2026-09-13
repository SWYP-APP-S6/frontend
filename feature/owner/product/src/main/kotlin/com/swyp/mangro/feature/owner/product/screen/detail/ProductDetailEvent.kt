package com.swyp.mangro.feature.owner.product.screen.detail

import com.swyp.mangro.feature.owner.product.model.OwnerProductModel

sealed interface ProductDetailEvent {
    data class SaveProduct(val product: OwnerProductModel) : ProductDetailEvent
    data object NavigateBack : ProductDetailEvent
    data class NavigateToCancellations(val productId: String) : ProductDetailEvent
}
