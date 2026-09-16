package com.swyp.mangro.data.owner.product.repository

import com.swyp.mangro.data.owner.product.model.HoldCancellations
import com.swyp.mangro.data.owner.product.model.HoldDetail
import com.swyp.mangro.data.owner.product.model.HoldPage
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedProduct
import kotlinx.coroutines.flow.Flow

interface OwnerProductRepository {
    fun fetchProduct(id: Long): Flow<Result<ManagedProduct>>
    fun updateStock(id: Long, quantity: Int): Flow<Result<ManagedProduct>>
    fun fetchHolds(page: Int, status: HoldStatus? = null): Flow<Result<HoldPage>>
    fun fetchHold(id: Long): Flow<Result<HoldDetail>>
    fun markAsPickedUp(id: Long): Flow<Result<HoldDetail>>
    fun fetchCancellations(): Flow<Result<HoldCancellations>>
    fun cancelHolds(ids: Set<Long>): Flow<Result<HoldCancellations>>
}
