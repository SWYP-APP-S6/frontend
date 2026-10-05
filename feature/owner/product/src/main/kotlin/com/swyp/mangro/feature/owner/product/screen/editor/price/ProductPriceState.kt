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

    val originalPriceError: ProductPriceError?
        get() = if (originalPrice.isNotEmpty() && (originalPriceValue ?: 0) <= 0) {
            ProductPriceError.INVALID_AMOUNT
        } else {
            null
        }

    val salePriceError: ProductPriceError?
        get() {
            if (salePrice.isEmpty()) return null
            val sale = salePriceValue ?: return ProductPriceError.INVALID_AMOUNT
            if (sale <= 0) return ProductPriceError.INVALID_AMOUNT
            val original = originalPriceValue
            return if (original != null && original > 0 && sale >= original) ProductPriceError.NOT_LOWER_THAN_ORIGINAL else null
        }

    val validPrices: Boolean get() = isValidPrice(originalPrice, salePrice)

    val canContinue: Boolean get() = validPrices && quantity > 0

    val discount: Int get() = discountPercent(originalPriceValue ?: 0, salePriceValue ?: 0)

    val savings: Int get() = if (validPrices) checkNotNull(originalPriceValue) - checkNotNull(salePriceValue) else 0
}

enum class ProductPriceError { INVALID_AMOUNT, NOT_LOWER_THAN_ORIGINAL }
