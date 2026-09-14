package com.swyp.mangro.feature.owner.product.model

import com.swyp.mangro.feature.owner.product.util.discountPercent
import java.io.Serializable

/** Remaining quantity includes stock allocated to active reservations. */
data class OwnerProductModel(
    val id: String,
    val name: String,
    val photos: List<String>,
    val originalPrice: Int,
    val salePrice: Int,
    val initialQuantity: Int,
    val remainingQuantity: Int,
    val reservedQuantity: Int = 0,
    val pickedUpQuantity: Int = 0,
    val pickupEndTime: String,
    val tags: List<String> = emptyList(),
) : Serializable {
    init {
        require(originalPrice > 0 && salePrice in 1..originalPrice)
        require(initialQuantity > 0 && remainingQuantity >= 0)
        require(reservedQuantity >= 0 && pickedUpQuantity >= 0)
    }

    val discountPercent: Int get() = discountPercent(originalPrice, salePrice)
    val shortageQuantity: Int get() = (reservedQuantity - remainingQuantity).coerceAtLeast(0)
    val availableQuantity: Int get() = (remainingQuantity - reservedQuantity).coerceAtLeast(0)
    val isVisibleToCustomers: Boolean get() = availableQuantity > 0
}
