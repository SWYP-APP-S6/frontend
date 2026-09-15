package com.swyp.mangro.feature.consumer.store.product

sealed interface ProductDetailUiAction {
    data object OnWishButtonClick : ProductDetailUiAction
    data class OnQuantityChange(val quantity: Int) : ProductDetailUiAction
    data object OnWishConfirmClick : ProductDetailUiAction
    data object OnWishBottomSheetDismiss : ProductDetailUiAction
    data object OnStoreInfoClick : ProductDetailUiAction
}
