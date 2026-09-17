package com.swyp.mangro.data.owner.product.repository

import androidx.paging.PagingData
import com.swyp.mangro.data.owner.product.model.HoldCancellations
import com.swyp.mangro.data.owner.product.model.HoldDetail
import com.swyp.mangro.data.owner.product.model.HoldPage
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedHold
import com.swyp.mangro.data.owner.product.model.ManagedProduct
import kotlinx.coroutines.flow.Flow

interface OwnerProductRepository {
    fun pagedHolds(status: HoldStatus?, onPageLoaded: (HoldPage) -> Unit): Flow<PagingData<ManagedHold>>
    fun refreshHolds()
    fun fetchProduct(id: Long): Flow<Result<ManagedProduct>>
    fun updateStock(id: Long, quantity: Int): Flow<Result<ManagedProduct>>
    fun fetchHolds(page: Int, status: HoldStatus? = null): Flow<Result<HoldPage>>
    fun fetchHold(id: Long): Flow<Result<HoldDetail>>
    fun markAsPickedUp(id: Long): Flow<Result<HoldDetail>>
    fun fetchCancellations(): Flow<Result<HoldCancellations>>
    fun cancelHolds(ids: Set<Long>): Flow<Result<HoldCancellations>>
}
