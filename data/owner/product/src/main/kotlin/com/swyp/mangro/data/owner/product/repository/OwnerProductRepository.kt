package com.swyp.mangro.data.owner.product.repository

import androidx.paging.PagingData
import com.swyp.mangro.data.owner.product.model.HoldCancellations
import com.swyp.mangro.data.owner.product.model.HoldDetail
import com.swyp.mangro.data.owner.product.model.HoldPage
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedHold
import com.swyp.mangro.data.owner.product.model.ManagedProduct
import com.swyp.mangro.data.owner.product.model.OwnerProductFilter
import com.swyp.mangro.data.owner.product.model.ProductPage
import com.swyp.mangro.data.owner.product.model.ProductSummary
import kotlinx.coroutines.flow.Flow

interface OwnerProductRepository {
    fun pagedProducts(
        filter: OwnerProductFilter = OwnerProductFilter.ALL,
        onPageLoaded: (ProductPage) -> Unit = {},
    ): Flow<PagingData<ProductSummary>>
    fun refreshProducts()
    fun fetchProducts(page: Int, filter: OwnerProductFilter = OwnerProductFilter.ALL): Flow<Result<ProductPage>>
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
