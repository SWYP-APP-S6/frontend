package com.swyp.mangro.data.owner.product.model

data class ProductSummary(
    val id: Long,
    val name: String,
    val photoUrl: String,
    val salePrice: Int,
    val availableQuantity: Int,
    val activeHoldQuantity: Long,
    val shortfallQuantity: Int,
)

data class ProductPage(val products: List<ProductSummary>, val total: Long, val last: Boolean)
