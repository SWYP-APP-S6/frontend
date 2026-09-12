package com.swyp.mangro.feature.owner.pickup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PickupTest {
    private fun pickup(id: String, quantity: Int = 1, requestedAt: Long = 1, productId: String = "peach", status: PickupStatus = PickupStatus.WAITING) = Pickup(id, productId, "복숭아 4입", "손님", quantity, 4_000, requestedAt, 1_000, status)

    @Test
    fun `입력 순서와 관계없이 먼저 찜한 순서대로 수량을 배정한다`() {
        val requests = listOf(pickup("late", requestedAt = 3), pickup("early", 2), pickup("middle", 2, 2))
        val shortage = pickupShortages(requests, mapOf("peach" to 3), 10).single()
        assertEquals(listOf("late", "middle"), shortage.targets.map { it.id })
        assertEquals(2L, shortage.missingQuantity)
        assertEquals(mapOf("early" to 1, "middle" to 2, "late" to 3), shortage.requestPositions)
    }

    @Test
    fun `동일 시각 찜은 id 순서로 일관되게 배정한다`() {
        val requests = listOf(pickup("b"), pickup("a"))
        assertEquals(listOf("b"), pickupShortages(requests, mapOf("peach" to 1), 10).single().targets.map { it.id })
    }

    @Test
    fun `상품별 재고와 부족 수량을 독립적으로 계산한다`() {
        val shortages = pickupShortages(listOf(pickup("a", 3), pickup("b", 2, productId = "apple")), mapOf("peach" to 1, "apple" to 0), 10)
        assertEquals(listOf(2L, 2L), shortages.map { it.missingQuantity })
        assertEquals(2, shortages.sumOf { it.targets.size })
    }

    @Test
    fun `재고가 정확히 충분하면 취소 대상이 없다`() {
        assertTrue(pickupShortages(listOf(pickup("a", 2), pickup("b")), mapOf("peach" to 3), 10).isEmpty())
    }

    @Test
    fun `재고 미조회는 품절로 오인하지 않고 픽업도 허용하지 않는다`() {
        val item = pickup("a")
        assertTrue(pickupShortages(listOf(item), emptyMap(), 10).isEmpty())
        assertFalse(canCompletePickup(item, listOf(item), emptyMap(), 10))
    }

    @Test
    fun `만료와 종료된 요청은 재고를 배정받지 않는다`() {
        val requests = PickupStatus.entries.filter { it != PickupStatus.WAITING }.map { pickup(it.name, status = it) }
        assertTrue(pickupShortages(requests, mapOf("peach" to 0), 10).isEmpty())
        assertTrue(pickupShortages(listOf(pickup("a")), mapOf("peach" to 0), 1_000).isEmpty())
    }

    @Test
    fun `만료 경계에서 대기 상태만 만료된다`() {
        assertEquals(PickupStatus.WAITING, pickup("a").statusAt(999))
        assertEquals(PickupStatus.EXPIRED, pickup("a").statusAt(1_000))
        assertEquals(PickupStatus.COMPLETED, pickup("a", status = PickupStatus.COMPLETED).statusAt(1_001))
    }

    @Test
    fun `필터는 픽업불가 고객취소 만료를 구분한다`() {
        val requests = PickupStatus.entries.map { pickup(it.name, status = it) }
        PickupFilter.entries.forEach { filter ->
            val filtered = filterPickups(requests, filter, 10)
            assertEquals(if (filter == PickupFilter.ALL) 5 else 1, filtered.size)
            assertTrue(filter.status == null || filtered.all { it.statusAt(10) == filter.status })
        }
        assertEquals(2, filterPickups(requests, PickupFilter.EXPIRED, 1_000).size)
    }

    @Test
    fun `재고 부족 및 완료 만료 요청은 픽업할 수 없다`() {
        val early = pickup("early", 2)
        val late = pickup("late", requestedAt = 2)
        assertTrue(canCompletePickup(early, listOf(early, late), mapOf("peach" to 2), 10))
        assertFalse(canCompletePickup(late, listOf(early, late), mapOf("peach" to 2), 10))
        assertFalse(canCompletePickup(early, listOf(early), mapOf("peach" to 2), 1_000))
        val done = early.copy(status = PickupStatus.COMPLETED)
        assertFalse(canCompletePickup(done, listOf(done), mapOf("peach" to 2), 10))
    }

    @Test
    fun `취소 후 남은 요청을 다시 배정하며 종료 요청은 취소 대상으로 복귀하지 않는다`() {
        val requests = listOf(pickup("a", 2), pickup("b", requestedAt = 2))
        val canceled = requests.map { if (it.id == "a") it.copy(status = PickupStatus.UNAVAILABLE) else it }
        assertTrue(pickupShortages(canceled, mapOf("peach" to 1), 10).isEmpty())
    }

    @Test
    fun `총액은 수량을 반영하고 Int 범위를 초과해도 올바르다`() {
        assertEquals(8_000L, pickup("a", 2).totalPrice)
        assertEquals(8_000_000_000L, pickup("a", 2_000_000).totalPrice)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `음수 재고는 거부한다`() {
        pickupShortages(emptyList(), mapOf("peach" to -1), 10)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `중복 요청 id는 거부한다`() {
        pickupShortages(listOf(pickup("a"), pickup("a")), mapOf("peach" to 1), 10)
    }
}
