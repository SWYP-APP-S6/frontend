package com.swyp.mangro.feature.owner.product.screen.editor.price

import com.swyp.mangro.feature.owner.product.util.discountPercent
import com.swyp.mangro.feature.owner.product.util.isValidPrice
import java.io.Serializable

data class ProductPriceState(
    val isLoading: Boolean = true,
    val originalPrice: String = "",
    val salePrice: String = "",
    val quantity: Int = 1,
) : Serializable {
    val validPrices: Boolean get() = isValidPrice(originalPrice, salePrice)
    val canContinue: Boolean get() = validPrices && quantity > 0
    val discount: Int get() = discountPercent(originalPrice.toIntOrNull() ?: 0, salePrice.toIntOrNull() ?: 0)
    val savings: Int get() = if (validPrices) originalPrice.toInt() - salePrice.toInt() else 0
}
