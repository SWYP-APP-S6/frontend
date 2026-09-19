package com.swyp.mangro.feature.owner.product.screen.editor.price

import com.swyp.mangro.feature.owner.product.util.discountPercent
import com.swyp.mangro.feature.owner.product.util.isValidPrice
import com.swyp.mangro.feature.owner.product.util.parsePrice
import java.io.Serializable

data class ProductPriceState(
    val originalPrice: String = "",
    val salePrice: String = "",
    val quantity: Int = 1,
) : Serializable {
    private val originalPriceValue: Int? get() = parsePrice(originalPrice)
    private val salePriceValue: Int? get() = parsePrice(salePrice)
    val validPrices: Boolean get() = isValidPrice(originalPrice, salePrice)
    val canContinue: Boolean get() = validPrices && quantity > 0
    val discount: Int get() = discountPercent(originalPriceValue ?: 0, salePriceValue ?: 0)
    val savings: Int get() = if (validPrices) checkNotNull(originalPriceValue) - checkNotNull(salePriceValue) else 0
}
