package com.swyp.mangro.feature.owner.product.util

object OwnerProductLimits {
    const val PHOTO_COUNT = 1
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

internal fun normalizePriceInput(input: String): String? = if (input.all { it in '0'..'9' || it == ',' }) {
    input.filter { it in '0'..'9' }
} else {
    null
}

internal fun parsePrice(input: String): Int? {
    val isPlainNumber = input.matches(PLAIN_PRICE_PATTERN)
    val isGroupedNumber = input.matches(GROUPED_PRICE_PATTERN)
    if (!isPlainNumber && !isGroupedNumber) return null
    return input.replace(",", "").toIntOrNull()
}

internal fun isValidPrice(original: String, sale: String): Boolean {
    val originalValue = parsePrice(original) ?: return false
    val saleValue = parsePrice(sale) ?: return false
    return originalValue > 0 && saleValue in 1..originalValue
}

internal fun mergedProductPhotos(current: List<String>, added: List<String>): List<String> = (current + added).distinct().take(OwnerProductLimits.PHOTO_COUNT)

internal fun parseQuantity(input: String): Int? = input.toIntOrNull()?.takeIf { it >= 0 }

private val PLAIN_PRICE_PATTERN = Regex("[0-9]+")
private val GROUPED_PRICE_PATTERN = Regex("[0-9]{1,3}(,[0-9]{3})+")
