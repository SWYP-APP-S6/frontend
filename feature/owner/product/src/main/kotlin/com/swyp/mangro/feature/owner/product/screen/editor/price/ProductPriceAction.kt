package com.swyp.mangro.feature.owner.product.screen.editor.price

sealed interface ProductPriceAction {
    data class OriginalPriceChanged(val value: String) : ProductPriceAction
    data class SalePriceChanged(val value: String) : ProductPriceAction
    data class QuantityChanged(val value: Int) : ProductPriceAction
    data object NextClicked : ProductPriceAction
    data object NavigationBackClicked : ProductPriceAction
}
