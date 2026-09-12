package com.swyp.mangro.feature.owner.pickup

/** 한 찜은 한 상품의 수량 단위이며, 부분 취소하지 않는다. */
data class Pickup(
    val id: String,
    val productId: String,
    val productName: String,
    val customerName: String,
    val quantity: Int,
    val unitPrice: Long,
    val requestedAt: Long,
    val deadline: Long,
    val status: PickupStatus = PickupStatus.WAITING,
    val completedAt: Long? = null,
) {
    init {
        require(id.isNotBlank() && productId.isNotBlank())
        require(quantity > 0 && unitPrice >= 0 && unitPrice <= Long.MAX_VALUE / quantity)
        require(deadline > requestedAt)
    }

    val totalPrice: Long get() = unitPrice * quantity

    fun statusAt(now: Long): PickupStatus = if (status == PickupStatus.WAITING && now >= deadline) PickupStatus.EXPIRED else status
}

enum class PickupStatus { WAITING, COMPLETED, UNAVAILABLE, CANCELED, EXPIRED }

enum class PickupFilter(val status: PickupStatus?) {
    ALL(null),
    COMPLETED(PickupStatus.COMPLETED),
    UNAVAILABLE(PickupStatus.UNAVAILABLE),
    CANCELED(PickupStatus.CANCELED),
    EXPIRED(PickupStatus.EXPIRED),
}

data class PickupShortage(
    val productId: String,
    val productName: String,
    val missingQuantity: Long,
    val targets: List<Pickup>,
    val requestPositions: Map<String, Int>,
)

/**
 * stock은 현재 실제 재고(이미 픽업한 수량 제외)이다. 누락 상품은 미조회로 취급한다.
 * 먼저 찜한 요청에 수량을 배정하며, 일부라도 부족한 요청은 전체 취소 대상이다.
 * 동일 시각은 id 순서로 고정한다. 서버 연동 시 서버의 순번/배정 결과가 최종 기준이다.
 */
fun pickupShortages(
    pickups: List<Pickup>,
    stock: Map<String, Int>,
    now: Long,
): List<PickupShortage> {
    require(stock.values.all { it >= 0 })
    require(pickups.map { it.id }.distinct().size == pickups.size)
    return pickups.filter { it.statusAt(now) == PickupStatus.WAITING }
        .groupBy { it.productId }
        .mapNotNull { (productId, requests) ->
            val available = stock[productId]?.toLong() ?: return@mapNotNull null
            val ordered = requests.sortedWith(compareBy<Pickup> { it.requestedAt }.thenBy { it.id })
            var demand = 0L
            val targets = ordered.filter {
                demand += it.quantity
                demand > available
            }
            if (targets.isEmpty()) {
                null
            } else {
                PickupShortage(
                    productId = productId,
                    productName = ordered.first().productName,
                    missingQuantity = demand - available,
                    targets = targets.asReversed(),
                    requestPositions = ordered.mapIndexed { index, pickup -> pickup.id to index + 1 }.toMap(),
                )
            }
        }
}

fun filterPickups(pickups: List<Pickup>, filter: PickupFilter, now: Long): List<Pickup> = pickups.filter { filter.status == null || it.statusAt(now) == filter.status }
    .sortedWith(compareByDescending<Pickup> { it.requestedAt }.thenBy { it.id })

fun canCompletePickup(pickup: Pickup, pickups: List<Pickup>, stock: Map<String, Int>, now: Long): Boolean = pickup.statusAt(now) == PickupStatus.WAITING &&
    stock.containsKey(pickup.productId) &&
    pickupShortages(pickups, stock, now).none { shortage -> shortage.targets.any { it.id == pickup.id } }
