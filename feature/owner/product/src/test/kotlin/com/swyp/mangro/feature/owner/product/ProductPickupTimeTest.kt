package com.swyp.mangro.feature.owner.product

import com.swyp.mangro.feature.owner.product.util.pickupTimeOptions
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductPickupTimeTest {
    @Test
    fun laterOfNowAndOpeningDeterminesFirstOption() {
        assertEquals(listOf("16:00", "17:00", "18:00"), pickupTimeOptions("16:00", "18:00", LocalTime.of(15, 20)))
        assertEquals(listOf("17:00", "18:00"), pickupTimeOptions("09:00", "18:00", LocalTime.of(16, 0)))
        assertEquals(listOf("17:00", "18:00"), pickupTimeOptions("16:30", "18:00", LocalTime.of(15, 20)))
    }

    @Test
    fun closingTimeIsIncludedEvenWhenNotOnTheHour() {
        assertEquals(listOf("20:00", "20:30"), pickupTimeOptions("09:00", "20:30", LocalTime.of(19, 59)))
    }

    @Test
    fun noOptionsAfterClosingOrWithInvalidHours() {
        assertTrue(pickupTimeOptions("09:00", "20:00", LocalTime.of(20, 0)).isEmpty())
        assertTrue(pickupTimeOptions("09:00", "20:00", LocalTime.of(23, 59)).isEmpty())
        assertTrue(pickupTimeOptions("", "20:00", LocalTime.NOON).isEmpty())
        assertTrue(pickupTimeOptions("20:00", "09:00", LocalTime.NOON).isEmpty())
    }
}
