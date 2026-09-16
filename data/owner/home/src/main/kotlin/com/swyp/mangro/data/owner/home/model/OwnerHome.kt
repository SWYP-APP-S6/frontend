package com.swyp.mangro.data.owner.home.model

data class OwnerHome(
    val storeId: Long,
    val storeStatus: String,
    val hasRegisteredProduct: Boolean,
    val upcomingVisitCount: Int,
    val completedTodayCount: Long,
    val onSaleQty: Int,
    val unreadNotificationCount: Long,
    val expiredTodayCount: Int,
    val productsShortOfStock: Int,
    val shortfallQty: Int,
    val reconfirmPendingCount: Int,
    val upcomingVisits: List<OwnerHomeVisit>,
    val products: List<OwnerHomeProduct>,
)

data class OwnerHomeVisit(
    val holdId: Long,
    val nickname: String,
    val summary: String,
    val totalQty: Int,
    val expiresAtMillis: Long,
)

data class OwnerHomeProduct(
    val id: Long,
    val name: String,
    val photoUrl: String,
    val salePrice: Int,
    val availableQty: Int,
    val activeHoldQty: Long,
    val shortfallQty: Int,
    val category: String,
    val status: String,
    val reconfirmPending: Boolean,
)
