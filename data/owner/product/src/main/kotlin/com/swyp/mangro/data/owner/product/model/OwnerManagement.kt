package com.swyp.mangro.data.owner.product.model

data class ManagedProduct(
    val id: Long,
    val name: String,
    val photoUrl: String,
    val originalPrice: Int,
    val salePrice: Int,
    val initialQuantity: Int,
    val stockQuantity: Int,
    val availableQuantity: Int,
    val activeHoldQuantity: Long,
    val completedQuantity: Long,
    val shortfallQuantity: Int,
    val pickupEndAt: Long,
    val ingredientTags: Set<Int>,
    val stockEditable: Boolean,
)

enum class HoldStatus { HOLDING, COMPLETED, EXPIRED, CANCELED_BY_OWNER, CANCELED_BY_USER }

data class ManagedHold(
    val id: Long,
    val groupId: Long,
    val productId: Long,
    val productName: String,
    val nickname: String,
    val quantity: Int,
    val heldAt: Long,
    val expiresAt: Long,
    val status: HoldStatus,
)

data class HoldPage(val holds: List<ManagedHold>, val total: Long, val last: Boolean, val serverTime: Long, val filteredTotal: Long = total)
data class HoldItem(val id: Long, val productId: Long, val name: String, val quantity: Int, val unitPrice: Int, val lineTotal: Int)
data class HoldDetail(
    val groupId: Long,
    val nickname: String,
    val storeName: String,
    val status: HoldStatus,
    val heldAt: Long,
    val expiresAt: Long,
    val serverTime: Long,
    val completedAt: Long?,
    val totalPrice: Int,
    val items: List<HoldItem>,
) {
    init {
        require(groupId > 0 && expiresAt > heldAt && totalPrice >= 0 && items.isNotEmpty())
        require(items.all { it.id > 0 && it.productId > 0 && it.quantity > 0 && it.unitPrice >= 0 && it.lineTotal >= 0 })
    }
}

data class CancellationCandidate(val id: Long, val order: Int, val heldAt: Long, val nickname: String, val quantity: Int, val suggested: Boolean)
data class CancellationProduct(val id: Long, val name: String, val shortfall: Int, val candidates: List<CancellationCandidate>)
data class HoldCancellations(val products: List<CancellationProduct>, val notice: String, val suggestedCount: Int)
