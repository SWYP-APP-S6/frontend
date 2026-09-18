package com.swyp.mangro.data.owner.product.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.swyp.mangro.data.owner.product.model.CancellationCandidate
import com.swyp.mangro.data.owner.product.model.CancellationProduct
import com.swyp.mangro.data.owner.product.model.HoldCancellations
import com.swyp.mangro.data.owner.product.model.HoldDetail
import com.swyp.mangro.data.owner.product.model.HoldItem
import com.swyp.mangro.data.owner.product.model.HoldPage
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedHold
import com.swyp.mangro.data.owner.product.model.ManagedProduct
import com.swyp.mangro.data.owner.product.model.ProductPage
import com.swyp.mangro.data.owner.product.model.ProductSummary
import com.swyp.mangro.data.owner.product.paging.OwnerHoldPagingSource
import com.swyp.mangro.data.owner.product.paging.OwnerProductPagingSource
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import com.swyp.mangro.remote.owner.model.CancelHoldsForShortageRequest
import com.swyp.mangro.remote.owner.model.OwnerHoldCancelCandidatesResponse
import com.swyp.mangro.remote.owner.model.OwnerHoldDetailResponse
import com.swyp.mangro.remote.owner.model.ProductDetailResponse
import com.swyp.mangro.remote.owner.model.UpdateStockRequest
import com.swyp.mangro.remote.owner.service.HoldService
import com.swyp.mangro.remote.owner.service.ProductService
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import retrofit2.Response

internal class OwnerProductRepositoryImpl @Inject constructor(
    private val products: ProductService,
    private val holds: HoldService,
) : OwnerProductRepository {
    private var holdPagingSource: OwnerHoldPagingSource? = null
    private var productPagingSource: OwnerProductPagingSource? = null

    override fun pagedProducts(): Flow<PagingData<ProductSummary>> = Pager(
        config = PagingConfig(
            pageSize = OwnerProductPagingSource.PAGE_SIZE,
            initialLoadSize = OwnerProductPagingSource.PAGE_SIZE,
            prefetchDistance = 5,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            OwnerProductPagingSource(this).also { productPagingSource = it }
        },
    ).flow

    override fun refreshProducts() {
        productPagingSource?.invalidate()
    }

    override fun fetchProducts(page: Int): Flow<Result<ProductPage>> = request {
        require(page >= 0)
        val body = products.fetchMyProducts(page = page, size = OwnerProductPagingSource.PAGE_SIZE).bodyOrThrow().products
        ProductPage(
            products = body.content.map {
                ProductSummary(
                    id = it.id,
                    name = it.name,
                    photoUrl = it.photoUrl,
                    salePrice = it.salePrice,
                    availableQuantity = it.availableQty,
                    activeHoldQuantity = it.activeHoldQty,
                    shortfallQuantity = it.shortfallQty,
                )
            },
            total = body.totalElements,
            last = body.last,
        )
    }

    override fun pagedHolds(status: HoldStatus?, onPageLoaded: (HoldPage) -> Unit): Flow<PagingData<ManagedHold>> = Pager(
        config = PagingConfig(
            pageSize = OwnerHoldPagingSource.PAGE_SIZE,
            initialLoadSize = OwnerHoldPagingSource.PAGE_SIZE,
            prefetchDistance = 5,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            OwnerHoldPagingSource(this, status, onPageLoaded).also { holdPagingSource = it }
        },
    ).flow

    override fun refreshHolds() {
        holdPagingSource?.invalidate()
    }

    override fun fetchProduct(id: Long): Flow<Result<ManagedProduct>> = request {
        require(id > 0)
        products.fetchMyProduct(id).bodyOrThrow().domain()
    }
    override fun updateStock(id: Long, quantity: Int): Flow<Result<ManagedProduct>> = request {
        require(id > 0 && quantity in 0..9999)
        products.updateStock(id, UpdateStockRequest(stockQty = quantity)).bodyOrThrow().domain()
    }
    override fun fetchHolds(page: Int, status: HoldStatus?): Flow<Result<HoldPage>> = request {
        require(page >= 0)
        val body = holds.fetchOwnerHolds(status = status?.let { HoldService.StatusFetchOwnerHolds.valueOf(it.name) }, page = page, size = OwnerHoldPagingSource.PAGE_SIZE).bodyOrThrow()
        HoldPage(
            body.holds.content.map {
                require(it.id > 0 && it.productId > 0)
                ManagedHold(it.id, it.groupId, it.productId, it.productName, it.nickname, it.qty, it.heldAt.epoch(), it.expiresAt.epoch(), HoldStatus.valueOf(it.status.value))
            },
            body.counts.all,
            body.holds.last,
            body.serverTime.epoch(),
            body.holds.totalElements,
        )
    }
    override fun fetchHold(id: Long): Flow<Result<HoldDetail>> = request {
        require(id > 0)
        holds.fetchOwnerHold(id).bodyOrThrow().domain()
    }
    override fun markAsPickedUp(id: Long): Flow<Result<HoldDetail>> = request {
        require(id > 0)
        holds.completePickup(id).bodyOrThrow().domain().also { require(it.status == HoldStatus.COMPLETED) }
    }
    override fun fetchCancellations(): Flow<Result<HoldCancellations>> = request {
        holds.fetchHoldCancelCandidates().bodyOrThrow().domain()
    }
    override fun cancelHolds(ids: Set<Long>): Flow<Result<HoldCancellations>> = request {
        require(ids.size in 1..100 && ids.all { it > 0 })
        holds.cancelHoldsForShortage(CancelHoldsForShortageRequest(holdIds = ids.toList())).bodyOrThrow().domain()
    }
}

private fun ProductDetailResponse.domain(): ManagedProduct {
    require(id > 0 && initialQty > 0 && stockQty >= 0 && originalPrice > 0 && salePrice in 1..originalPrice)
    require(activeHoldQty >= 0 && completedQty >= 0 && shortfallQty >= 0 && minAdjustableQty in 0..9999)
    return ManagedProduct(id, name, photoUrl, originalPrice, salePrice, initialQty, stockQty, availableQty, activeHoldQty, completedQty, shortfallQty, pickupEndAt.epoch(), ingredientTags.map { it.id }.toSet(), stockEditable, minAdjustableQty)
}
private fun OwnerHoldDetailResponse.domain() = HoldDetail(
    groupId, nickname, storeName, HoldStatus.valueOf(status.value), heldAt.epoch(), expiresAt.epoch(), serverTime.epoch(), completedAt?.epoch(), totalPrice,
    items.map { HoldItem(it.holdId, it.productId, it.productName, it.qty, it.unitPrice, it.lineTotal) },
)
private fun OwnerHoldCancelCandidatesResponse.domain() = HoldCancellations(
    products.map { product ->
        CancellationProduct(
            product.productId,
            product.productName,
            product.shortfallQty,
            product.holds.map {
                CancellationCandidate(it.holdId, it.heldOrder, it.heldAt.epoch(), it.nickname, it.qty, it.suggested)
            },
        )
    },
    noticeMessage,
    suggestedCancelCount,
)
private fun String.epoch() = OffsetDateTime.parse(this).toInstant().toEpochMilli()
private fun <T> Response<T>.bodyOrThrow(): T {
    if (!isSuccessful) throw HttpException(this)
    return requireNotNull(body())
}
private fun <T> request(block: suspend () -> T): Flow<Result<T>> = flow {
    emit(
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        },
    )
}.flowOn(Dispatchers.IO)
