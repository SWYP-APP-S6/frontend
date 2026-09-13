package com.swyp.mangro.feature.owner.product.model

import java.io.Serializable

data class ProductDraftModel(
    val id: String,
    val name: String = "",
    val photos: List<String> = emptyList(),
    val originalPrice: Int = 0,
    val salePrice: Int = 0,
    val quantity: Int = 1,
) : Serializable
