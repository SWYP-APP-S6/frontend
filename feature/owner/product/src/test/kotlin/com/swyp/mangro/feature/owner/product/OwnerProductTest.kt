package com.swyp.mangro.feature.owner.product

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerProductTest {
    @Test
    fun `prices reject zero negative overflow and higher sale prices`() {
        assertTrue(isValidPrice("10000", "4000"))
        assertTrue(isValidPrice("100", "100"))
        listOf("0" to "0", "-1" to "1", "100" to "101", "100" to "", "2147483648" to "1").forEach { (original, sale) ->
            assertFalse(isValidPrice(original, sale))
        }
        assertEquals(60, discountPercent(10000, 4000))
        assertEquals(66, discountPercent(3, 1))
        assertEquals(99, discountPercent(Int.MAX_VALUE, 1))
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
    fun `tags trim deduplicate and enforce five while allowing removal`() {
        assertEquals(listOf("청과"), addProductTag(emptyList(), " 청과 "))
        assertEquals(listOf("청과"), addProductTag(listOf("청과"), "청과"))
        assertTrue(addProductTag(emptyList(), " ").isEmpty())
        val full = listOf("a", "b", "c", "d", "e")
        assertEquals(full, addProductTag(full, "f"))
        assertEquals(listOf("b", "c", "d", "e", "f"), addProductTag(full - "a", "f"))
    }

    @Test
    fun `photo additions cannot duplicate or exceed five`() {
        assertEquals(listOf("a", "b", "c", "d", "e"), mergedProductPhotos(listOf("a", "b"), listOf("b", "c", "d", "e", "f")))
    }

    @Test
    fun `quantity parsing accepts zero and rejects invalid values`() {
        assertEquals(0, parseQuantity("0"))
        assertEquals(Int.MAX_VALUE, parseQuantity(Int.MAX_VALUE.toString()))
        listOf("", "-1", "1.5", "2147483648", "1a").forEach { assertNull(parseQuantity(it)) }
    }

    @Test
    fun `physical stock includes reservations and never exposes negative availability`() {
        val product = OwnerProduct("1", "복숭아", listOf("photo"), 10000, 4000, 10, 5, 3, 2, "20:00")
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
