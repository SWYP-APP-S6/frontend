package com.swyp.mangro.data.owner.product.repository

import com.swyp.mangro.data.owner.product.model.ProductRegistration
import com.swyp.mangro.data.owner.product.model.RegisteredProduct
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun register(product: ProductRegistration): Flow<Result<RegisteredProduct>>
}
