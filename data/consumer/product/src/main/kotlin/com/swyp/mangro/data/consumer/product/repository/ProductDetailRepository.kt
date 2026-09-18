package com.swyp.mangro.data.consumer.product.repository

import com.swyp.mangro.data.consumer.product.model.ProductDetail
import kotlinx.coroutines.flow.Flow

interface ProductDetailRepository {
    fun fetchProduct(productId: Long, lat: Double? = null, lng: Double? = null): Flow<Result<ProductDetail>>
    fun registerHold(productId: Long, qty: Int): Flow<Result<Long>>
}
