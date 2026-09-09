package com.swyp.mangro.feature.owner.product

import java.io.Serializable

/** UI input limits taken from the filled O-020 handoff; see the module README. */
object OwnerProductLimits {
    const val PHOTO_COUNT = 5
    const val TAG_COUNT = 5
    const val NAME_LENGTH = 25
}

/** Remaining quantity includes stock allocated to active reservations. */
data class OwnerProduct(
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

internal fun discountPercent(originalPrice: Int, salePrice: Int): Int = if (originalPrice > 0 && salePrice in 1..originalPrice) {
    ((originalPrice.toLong() - salePrice) * 100 / originalPrice).toInt()
} else {
    0
}

internal fun isValidProductName(name: String): Boolean = name.trim().let {
    it.isNotEmpty() && it.codePointCount(0, it.length) <= OwnerProductLimits.NAME_LENGTH
}

internal fun isValidPrice(original: String, sale: String): Boolean {
    val originalValue = original.toIntOrNull() ?: return false
    val saleValue = sale.toIntOrNull() ?: return false
    return originalValue > 0 && saleValue in 1..originalValue
}

internal fun addProductTag(tags: List<String>, input: String): List<String> {
    val tag = input.trim()
    return if (tag.isNotEmpty() && tag !in tags && tags.size < OwnerProductLimits.TAG_COUNT) tags + tag else tags
}

internal fun mergedProductPhotos(current: List<String>, added: List<String>): List<String> = (current + added).distinct().take(OwnerProductLimits.PHOTO_COUNT)

internal fun parseQuantity(input: String): Int? = input.toIntOrNull()?.takeIf { it >= 0 }
