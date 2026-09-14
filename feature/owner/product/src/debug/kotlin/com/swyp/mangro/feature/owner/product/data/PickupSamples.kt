package com.swyp.mangro.feature.owner.product.data

import com.swyp.mangro.feature.owner.product.model.Pickup
import com.swyp.mangro.feature.owner.product.model.PickupStatus

internal fun initialPickupSnapshot() = PickupSnapshot(
    pickups = samplePickups(System.currentTimeMillis()),
    stock = mapOf("peach" to 1, "apple" to 1, "orange" to 3),
    storeName = "맹그로청과",
    storePhone = "02-123-4567",
    isDemo = true,
)

private fun samplePickups(now: Long): List<Pickup> {
    fun pickup(id: String, product: String, customer: String, quantity: Int, minutesAgo: Int, status: PickupStatus = PickupStatus.WAITING): Pickup {
        val requestedAt = now - minutesAgo * 60_000L
        return Pickup(
            id = id,
            productId = product,
            productName = when (product) {
                "peach" -> "복숭아 4입"
                "apple" -> "아오리사과 6입(특)"
                else -> "오렌지 1망"
            },
            customerName = customer,
            quantity = quantity,
            unitPrice = 4_000,
            requestedAt = requestedAt,
            deadline = requestedAt + 15 * 60_000,
            status = status,
            completedAt = if (status == PickupStatus.COMPLETED) now - 60_000 else null,
            notificationsEnabled = id != "2",
            isNew = id == "7",
        )
    }
    return listOf(
        pickup("1", "peach", "김민수", 1, 10),
        pickup("2", "peach", "송유나", 2, 8),
        pickup("3", "peach", "윤지현", 1, 4),
        pickup("4", "apple", "이수진", 1, 10),
        pickup("5", "apple", "건우건어물", 2, 8),
        pickup("6", "apple", "맛있으면짖는개", 1, 4),
        pickup("7", "orange", "단골손님", 2, 1),
        pickup("8", "orange", "양모펠트", 1, 20),
        pickup("9", "orange", "수령손님", 1, 30, PickupStatus.COMPLETED),
        pickup("10", "orange", "취소손님", 1, 40, PickupStatus.CANCELED),
    )
}
