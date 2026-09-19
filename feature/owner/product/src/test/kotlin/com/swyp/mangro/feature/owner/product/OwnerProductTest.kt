package com.swyp.mangro.feature.owner.product

import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.editor.price.ProductPriceState
import com.swyp.mangro.feature.owner.product.util.discountPercent
import com.swyp.mangro.feature.owner.product.util.isValidPrice
import com.swyp.mangro.feature.owner.product.util.isValidProductName
import com.swyp.mangro.feature.owner.product.util.mergedProductPhotos
import com.swyp.mangro.feature.owner.product.util.normalizePriceInput
import com.swyp.mangro.feature.owner.product.util.parsePrice
import com.swyp.mangro.feature.owner.product.util.parseQuantity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerProductTest {
    @Test
    fun `prices reject zero negative overflow and equal or higher sale prices`() {
        assertTrue(isValidPrice("10000", "4000"))
        assertTrue(isValidPrice("10,000", "5,000"))
        listOf("0" to "0", "-1" to "1", "100" to "100", "100" to "101", "100" to "", "2147483648" to "1", "10,,000" to "5,000").forEach { (original, sale) ->
            assertFalse(isValidPrice(original, sale))
        }
        assertEquals("5000", normalizePriceInput("5,000"))
        assertNull(normalizePriceInput("5천원"))
        assertEquals(5000, parsePrice("5,000"))
        assertEquals(60, discountPercent(10000, 4000))
        assertEquals(66, discountPercent(3, 1))
        assertEquals(99, discountPercent(Int.MAX_VALUE, 1))
    }

    @Test
    fun `equal original and sale prices cannot continue product registration`() {
        val state = ProductPriceState(originalPrice = "10,000", salePrice = "10,000")

        assertFalse(state.validPrices)
        assertFalse(state.canContinue)
        assertEquals(0, state.discount)
        assertEquals(0, state.savings)
    }

    @Test
    fun `name counts unicode codepoints and ignores surrounding whitespace`() {
        assertFalse(isValidProductName("   "))
        assertTrue(isValidProductName(" 복숭아 4입 "))
        assertTrue(isValidProductName("가".repeat(25)))
        assertFalse(isValidProductName("가".repeat(26)))
        assertTrue(isValidProductName("🍑".repeat(25)))
    }

    @Test
    fun `photo selection keeps only one and allows selection after removal`() {
        assertEquals(listOf("1"), mergedProductPhotos(emptyList(), listOf("1", "2")))
        assertEquals(listOf("1"), mergedProductPhotos(listOf("1"), listOf("1", "2")))
        assertEquals(listOf("2"), mergedProductPhotos(listOf("1") - "1", listOf("2")))
        assertTrue(mergedProductPhotos(emptyList(), emptyList()).isEmpty())
    }

    @Test
    fun `quantity parsing accepts zero and rejects invalid values`() {
        assertEquals(0, parseQuantity("0"))
        assertEquals(Int.MAX_VALUE, parseQuantity(Int.MAX_VALUE.toString()))
        listOf("", "-1", "1.5", "2147483648", "1a").forEach { assertNull(parseQuantity(it)) }
    }

    @Test
    fun `physical stock includes reservations and never exposes negative availability`() {
        val product = OwnerProductModel("1", "복숭아", listOf("photo"), 10000, 4000, 10, 5, 3, 2, "20:00")
        assertEquals(2, product.availableQuantity)
        assertTrue(product.isVisibleToCustomers)
        val shortage = product.copy(remainingQuantity = 1)
        assertEquals(2, shortage.shortageQuantity)
        assertEquals(0, shortage.availableQuantity)
        assertFalse(shortage.isVisibleToCustomers)
        assertFalse(product.copy(remainingQuantity = 0, reservedQuantity = 0).isVisibleToCustomers)
        assertEquals(3, product.reservedQuantity)
    }
}
