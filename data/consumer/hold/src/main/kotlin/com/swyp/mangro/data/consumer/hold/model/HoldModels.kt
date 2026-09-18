package com.swyp.mangro.data.consumer.hold.model

data class HoldSummary(
    val id: Long,
    val status: String,
    val storeId: Long,
    val storeName: String,
    val productId: Long,
    val productName: String,
    val photoUrl: String,
    val qty: Int,
    val totalPrice: Int,
    val heldAtMillis: Long,
    val expiresAtMillis: Long,
    val completedAtMillis: Long?,
    val canceledAtMillis: Long?,
)

data class HoldHistory(
    val holds: List<HoldSummary>,
    val totalPages: Int,
    val last: Boolean,
)

data class HoldDetail(
    val id: Long,
    val status: String,
    val totalQty: Int,
    val totalPrice: Int,
    val heldAtMillis: Long,
    val expiresAtMillis: Long,
    val completedAtMillis: Long?,
    val store: HoldStore,
    val items: List<HoldItem>,
)

data class HoldStore(
    val id: Long,
    val name: String,
    val address: String,
    val addressDetail: String?,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val businessOpenTime: String,
    val businessCloseTime: String,
    val openNow: Boolean,
)

data class HoldItem(
    val holdId: Long,
    val productId: Long,
    val name: String,
    val photoUrl: String,
    val originalPrice: Int,
    val salePrice: Int,
    val discountRate: Int?,
    val status: String,
    val qty: Int,
    val lineTotal: Int,
)
