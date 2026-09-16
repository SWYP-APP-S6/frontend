package com.swyp.mangro.core.model.product

data class Product(
    val id: String,
    val imageUrl: String,
    val discountRate: Int?,
    val name: String,
    val price: Int,
    val originalPrice: Int? = null,
    val category: ProductCategory,
    val remainingCount: Int,
)
