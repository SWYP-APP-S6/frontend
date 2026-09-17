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
    val reservedQuantity: Long = 0,
    val pickedUpQuantity: Long = 0,
    val pickupEndTime: String,
    val tags: List<String> = emptyList(),
    val tagsResolved: Boolean = true,
    val serverShortfall: Int? = null,
    val serverAvailable: Int? = null,
    val stockEditable: Boolean = true,
    val minAdjustableQuantity: Int = 0,
) : Serializable {
    init {
        require(originalPrice > 0 && salePrice in 1..originalPrice)
        require(initialQuantity > 0 && remainingQuantity >= 0)
        require(reservedQuantity >= 0 && pickedUpQuantity >= 0)
    }

    val discountPercent: Int get() = discountPercent(originalPrice, salePrice)
    val shortageQuantity: Int get() = serverShortfall ?: (reservedQuantity - remainingQuantity).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
    val availableQuantity: Int get() = serverAvailable ?: (remainingQuantity - reservedQuantity).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
    val isVisibleToCustomers: Boolean get() = availableQuantity > 0
}
