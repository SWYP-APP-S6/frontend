package com.swyp.mangro.feature.owner.product.util

object OwnerProductLimits {
    const val PHOTO_COUNT = 5
    const val TAG_COUNT = 5
    const val NAME_LENGTH = 25
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
