package com.swyp.mangro.feature.owner.product.model

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
    val notificationsEnabled: Boolean = true,
    val isNew: Boolean = false,
) {
    init {
        require(id.isNotBlank() && productId.isNotBlank())
        require(quantity > 0 && unitPrice >= 0 && unitPrice <= Long.MAX_VALUE / quantity)
        require(deadline > requestedAt)
    }

    fun statusAt(now: Long): PickupStatus = if (status == PickupStatus.WAITING && now >= deadline) PickupStatus.EXPIRED else status
}

enum class PickupStatus { WAITING, COMPLETED, UNAVAILABLE, CANCELED, EXPIRED }
