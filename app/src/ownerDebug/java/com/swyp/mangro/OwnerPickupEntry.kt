package com.swyp.mangro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.swyp.mangro.feature.owner.pickup.OwnerPickupRoute
import com.swyp.mangro.feature.owner.pickup.Pickup
import com.swyp.mangro.feature.owner.pickup.PickupStatus
import com.swyp.mangro.feature.owner.pickup.canCompletePickup
import com.swyp.mangro.feature.owner.pickup.pickupShortages

/** API 연결 전 UI 검증용. 이 데이터와 로컬 처리는 OwnerDebug에만 포함된다. */
@Composable
internal fun OwnerPickupEntry(onHomeClick: () -> Unit) {
    val startedAt = rememberSaveable { System.currentTimeMillis() }
    var completedIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var canceledIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val initial = samplePickups(startedAt)
    val pickups = initial.map {
        it.copy(
            status = when (it.id) {
                in completedIds -> PickupStatus.COMPLETED
                in canceledIds -> PickupStatus.UNAVAILABLE
                else -> it.status
            },
        )
    }
    val stock = mapOf("peach" to 1, "apple" to 1, "orange" to 3).mapValues { (id, amount) ->
        amount - initial.filter { it.productId == id && it.id in completedIds }.sumOf { it.quantity }
    }
    OwnerPickupRoute(
        pickups = pickups,
        stock = stock,
        storeName = "맹그로청과",
        storePhone = "02-123-4567",
        onCompletePickup = { id ->
            val target = pickups.single { it.id == id }
            check(canCompletePickup(target, pickups, stock, System.currentTimeMillis()))
            completedIds = completedIds + id
        },
        onCancelPickups = { ids, _ ->
            val targets = pickupShortages(pickups, stock, System.currentTimeMillis()).flatMap { it.targets }.map { it.id }.toSet()
            check(ids.isNotEmpty() && targets.containsAll(ids))
            canceledIds = canceledIds + ids
        },
        onHomeClick = onHomeClick,
    )
}

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

internal const val IS_PICKUP_DEMO = true
