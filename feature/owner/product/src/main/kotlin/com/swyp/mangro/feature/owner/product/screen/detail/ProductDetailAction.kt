package com.swyp.mangro.feature.owner.product.screen.detail

sealed interface ProductDetailAction {
    data class QuantityChanged(val quantity: Int) : ProductDetailAction
    data object Save : ProductDetailAction
    data object ConfirmSave : ProductDetailAction
    data object DismissConfirmation : ProductDetailAction
    data object DismissSaved : ProductDetailAction
    data object CompleteSave : ProductDetailAction
    data object NavigateBack : ProductDetailAction
    data object EditProduct : ProductDetailAction
    data object CancelReservations : ProductDetailAction
}
