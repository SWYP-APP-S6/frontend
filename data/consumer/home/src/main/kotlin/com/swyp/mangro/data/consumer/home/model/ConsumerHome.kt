package com.swyp.mangro.data.consumer.home.model

data class MyLocation(
    val regionName: String,
    val latitude: Double,
    val longitude: Double,
    val updatedAtMillis: Long?,
)

data class NearbyStores(
    val totalStoreCount: Int,
    val truncated: Boolean,
    val stores: List<NearbyStore>,
)

data class NearbyStore(
    val storeId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val sellableProductCount: Int,
)

data class StoreDetail(
    val storeId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int?,
    val walkingMinutes: Int?,
    val businessCloseTime: String?,
    val earliestPickupEndAtMillis: Long?,
    val productCount: Int,
    val products: List<StoreDetailProduct>,
)

data class StoreDetailProduct(
    val id: Long,
    val name: String,
    val photoUrl: String,
    val category: String,
    val originalPrice: Int,
    val salePrice: Int,
    val discountRate: Int?,
    val availableQty: Int,
    val pickupEndAtMillis: Long?,
)

data class NearbyProducts(
    val totalProductCount: Long,
    val page: Int,
    val totalPages: Int,
    val last: Boolean,
    val storeGroups: List<NearbyStoreGroup>,
)

data class NearbyStoreGroup(
    val storeId: Long,
    val storeName: String,
    val distanceMeters: Int,
    val walkingMinutes: Int,
    val productCount: Int,
    val hasMoreProducts: Boolean,
    val earliestPickupEndAtMillis: Long?,
    val products: List<StoreDetailProduct>,
)

enum class ProductSortOption { DISTANCE, PICKUP_DEADLINE, DISCOUNT_RATE }

data class ActiveHold(
    val holdId: Long,
    val storeName: String,
    val firstItemName: String,
    val totalQty: Int,
    val heldAtMillis: Long,
    val expiresAtMillis: Long,
)
