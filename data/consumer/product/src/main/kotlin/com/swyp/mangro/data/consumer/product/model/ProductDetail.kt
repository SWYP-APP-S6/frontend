package com.swyp.mangro.data.consumer.product.model

data class ProductDetail(
    val id: Long,
    val name: String,
    val category: String,
    val tags: List<String>,
    val photoUrls: List<String>,
    val originalPrice: Int,
    val salePrice: Int,
    val discountRate: Int?,
    val availableQty: Int,
    val status: String,
    val holdButton: String,
    val myHoldId: Long?,
    val store: ProductStore,
    val recipes: List<ProductRecipe>,
)

data class ProductStore(
    val id: Long,
    val name: String,
    val address: String,
    val addressDetail: String?,
    val phone: String,
    val distanceMeters: Int?,
    val walkingMinutes: Int?,
    val businessCloseTime: String,
)

data class ProductRecipe(
    val id: Long,
    val title: String,
    val difficulty: String?,
    val ingredientNames: List<String>,
)
