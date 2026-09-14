package com.swyp.mangro

import com.swyp.mangro.feature.owner.product.data.PickupSnapshot
import com.swyp.mangro.feature.owner.product.model.Pickup
import com.swyp.mangro.feature.owner.product.model.PickupStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerHomePickupMapperTest {
    @Test
    fun homeUsesTheSameOrderIdsAndStockEligibilityAsPickupDetails() {
        val snapshot = PickupSnapshot(
            pickups = listOf(order("first"), order("second").copy(requestedAt = 200)),
            stock = mapOf("peach" to 1),
            isDemo = true,
        )

        val home = snapshot.toHomePickups(500)

        assertEquals(listOf("first", "second"), home.visitors.map { it.id })
        assertTrue(home.visitors.first().canComplete)
        assertFalse(home.visitors.last().canComplete)
        assertEquals(1, home.cancellationRequiredCount)
    }

    @Test
    fun completedCancelledAndExpiredOrdersAreExcludedFromUpcomingVisitors() {
        val home = PickupSnapshot(
            pickups = listOf(
                order("waiting").copy(isNew = true),
                order("completed").copy(status = PickupStatus.COMPLETED),
                order("cancelled").copy(status = PickupStatus.CANCELED),
                order("expired").copy(deadline = 400),
            ),
            stock = mapOf("peach" to 10),
        ).toHomePickups(500)

        assertEquals(listOf("waiting"), home.visitors.map { it.id })
        assertEquals(1, home.completedCount)
        assertTrue(home.needsPickupConfirmation)
        assertTrue(home.hasNewPickup)
        assertFalse(home.visitors.single().canComplete)
    }

    private fun order(id: String) = Pickup(id, "peach", "복숭아", id, 1, 4_000, 100, 1_000)
}
