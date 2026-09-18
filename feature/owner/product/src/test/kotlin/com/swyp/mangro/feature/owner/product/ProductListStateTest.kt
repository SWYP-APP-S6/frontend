package com.swyp.mangro.feature.owner.product

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestItem
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestStatus
import com.swyp.mangro.data.owner.product.model.OwnerProductFilter
import com.swyp.mangro.feature.owner.product.model.OwnerPickupModel
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.list.ProductListFilter
import com.swyp.mangro.feature.owner.product.screen.list.ProductListState
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductListStateTest {
    private val peach = OwnerProductModel("peach", "복숭아", emptyList(), 5000, 4000, 10, 1, 8, 0, "20:00")
    private fun pickup(id: String, status: OwnerPickupRequestStatus, quantity: Int = 1) = OwnerPickupModel(
        "peach",
        OwnerPickupRequestItem(id, "09.02(수) 18시 20분", "방문자", "18시 35분까지 픽업 예정", "복숭아", quantity, status),
    )

    @Test
    fun shortageUnitsAreNotCancellationOrCustomerCounts() {
        val state = ProductListState(products = listOf(peach))
        assertEquals(7, peach.shortageQuantity)
        assertEquals(0, state.cancellationNeeded.size)
        assertEquals(0, state.pickups.size)
    }

    @Test
    fun cancellationCountUsesReservationRecordsEvenForMultipleQuantities() {
        val state = ProductListState(pickups = listOf(pickup("a", OwnerPickupRequestStatus.UNAVAILABLE, 7), pickup("b", OwnerPickupRequestStatus.CANCELLED)))
        assertEquals(listOf("a"), state.cancellationNeeded.map { it.request.id })
    }

    @Test
    fun eachPickupFilterUsesMatchingReservationStatus() {
        val pickups = OwnerPickupRequestStatus.entries.map { pickup(it.name, it) }
        ProductListFilter.entries.filter { it != ProductListFilter.ALL }.forEach { filter ->
            val state = ProductListState(products = listOf(peach, peach.copy(id = "other")), pickups = pickups, filter = filter)
            assertEquals(listOf(filter.status), state.filteredPickups.map { it.request.status })
        }
    }

    @Test
    fun productAndPickupFiltersAreStoredIndependently() {
        val state = ProductListState(
            productFilter = OwnerProductFilter.SOLD_OUT,
            filter = ProductListFilter.COMPLETED,
        )
        assertEquals(OwnerProductFilter.SOLD_OUT, state.productFilter)
        assertEquals(ProductListFilter.COMPLETED, state.filter)
    }
}
