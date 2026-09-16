package com.swyp.mangro.data.owner.product.model

data class ProductRegistration(
    val name: String,
    val photos: List<String>,
    val quantity: Int,
    val originalPrice: Int,
    val salePrice: Int,
    val pickupEndAt: String,
    val category: String,
    val ingredientTags: List<Int> = emptyList(),
)

data class RegisteredProduct(
    val id: Long,
    val name: String,
    val photoUrl: String,
    val originalPrice: Int,
    val salePrice: Int,
    val initialQuantity: Int,
    val stockQuantity: Int,
    val heldQuantity: Int,
    val completedQuantity: Int,
    val pickupEndAt: String,
)
