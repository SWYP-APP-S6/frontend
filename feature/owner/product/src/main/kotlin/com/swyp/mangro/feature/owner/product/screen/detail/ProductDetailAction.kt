package com.swyp.mangro.feature.owner.product.screen.detail

sealed interface ProductDetailAction {
    data object RetryClicked : ProductDetailAction
    data class QuantityChanged(val quantity: Int) : ProductDetailAction
    data object SaveClicked : ProductDetailAction
    data object SaveConfirmClicked : ProductDetailAction
    data object SaveConfirmationDismissed : ProductDetailAction
    data object SaveResultDismissed : ProductDetailAction
    data object SaveResultConfirmClicked : ProductDetailAction
    data object NavigationBackClicked : ProductDetailAction
    data object ReservationsCancelClicked : ProductDetailAction
}
