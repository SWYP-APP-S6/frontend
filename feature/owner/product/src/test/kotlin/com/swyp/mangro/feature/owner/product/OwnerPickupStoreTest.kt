package com.swyp.mangro.feature.owner.product

import com.swyp.mangro.feature.owner.product.data.OwnerPickupStore
import com.swyp.mangro.feature.owner.product.data.PickupSnapshot
import com.swyp.mangro.feature.owner.product.model.Pickup
import com.swyp.mangro.feature.owner.product.model.PickupStatus
import com.swyp.mangro.feature.owner.product.model.presentation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerPickupStoreTest {
    private fun pickup(id: String, time: Long, quantity: Int = 1) = Pickup(
        id = id,
        productId = "peach",
        productName = "복숭아 4입",
        customerName = id,
        quantity = quantity,
        unitPrice = 3000,
        requestedAt = time,
        deadline = time + 900_000,
    )

    @Test
    fun completionUpdatesBothStatusAndStockAndCannotRunTwice() {
        val store = OwnerPickupStore(PickupSnapshot(listOf(pickup("first", 0)), mapOf("peach" to 3), isDemo = true))
        assertTrue(store.complete("first", 100))
        assertFalse(store.complete("first", 101))
        assertEquals(2, store.snapshot.value.stock["peach"])
        assertEquals(100L, store.snapshot.value.pickups.single().completedAt)
        assertEquals(PickupStatus.COMPLETED, store.snapshot.value.pickups.single().status)
    }

    @Test
    fun cancellationCannotIncludeAllocatedOrExpiredRequests() {
        val store = OwnerPickupStore(PickupSnapshot(listOf(pickup("first", 0), pickup("later", 1)), mapOf("peach" to 1), isDemo = true))
        assertFalse(store.cancel(setOf("first", "later"), 100))
        assertEquals(2, store.snapshot.value.pickups.count { it.status == PickupStatus.WAITING })
        assertFalse(store.cancel(setOf("later"), 900_001))
        assertTrue(store.cancel(setOf("later"), 100))
        assertFalse(store.cancel(setOf("later"), 101))
    }

    @Test
    fun canceledShortageRemainsUnavailableButNoLongerAppearsInNotice() {
        val store = OwnerPickupStore(PickupSnapshot(listOf(pickup("a", 0, 2)), mapOf("peach" to 1), isDemo = true))
        assertTrue(store.snapshot.value.presentation(100).single().needsCancellation)
        assertTrue(store.cancel(setOf("a"), 100))
        val item = store.snapshot.value.presentation(100).single()
        assertFalse(item.needsCancellation)
        assertFalse(item.canComplete)
        assertEquals(PickupStatus.UNAVAILABLE, store.snapshot.value.pickups.single().status)
        assertEquals(1, store.snapshot.value.stock["peach"])
    }

    @Test
    fun releaseSnapshotNeverSimulatesCompletionOrSendingMessages() {
        val store = OwnerPickupStore(PickupSnapshot(listOf(pickup("a", 0)), mapOf("peach" to 1)))
        assertFalse(store.complete("a", 100))
        assertFalse(store.cancel(setOf("a"), 100))
        assertEquals(PickupStatus.WAITING, store.snapshot.value.pickups.single().status)
    }

    @Test
    fun expiryChangesPresentationAndRemovesCancellationNotice() {
        val store = OwnerPickupStore(PickupSnapshot(listOf(pickup("a", 0)), mapOf("peach" to 0), isDemo = true))
        assertTrue(store.snapshot.value.presentation(899_999).single().needsCancellation)
        assertFalse(store.snapshot.value.presentation(900_000).single().needsCancellation)
        assertFalse(store.complete("a", 900_000))
    }
}
