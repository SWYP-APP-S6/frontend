package com.swyp.mangro.feature.owner.product

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
import com.swyp.mangro.data.owner.product.model.ProductPage
import com.swyp.mangro.data.owner.product.model.ProductSummary
import com.swyp.mangro.data.owner.product.model.ManagedProduct
import com.swyp.mangro.data.owner.product.paging.OwnerHoldPagingSource
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class ManagementFakeRepository : OwnerProductRepository {
    override fun pagedProducts() = flowOf(PagingData.empty<ProductSummary>())
    override fun refreshProducts() = Unit
    override fun fetchProducts(page: Int) = flowOf(Result.success(ProductPage(emptyList(), 0, true)))
    private var source: OwnerHoldPagingSource? = null
    override fun pagedHolds(status: HoldStatus?, onPageLoaded: (HoldPage) -> Unit) = Pager(
        PagingConfig(pageSize = 100, initialLoadSize = 100, prefetchDistance = 5, enablePlaceholders = false),
    ) { OwnerHoldPagingSource(this, status, onPageLoaded).also { source = it } }.flow
    override fun refreshHolds() {
        source?.invalidate()
    }

    private val now = System.currentTimeMillis()
    var failed = false
    var writes = 0
    var cancellationReads = 0
    var productReadGate: CompletableDeferred<Unit>? = null
    var productReads = 0
    var detailReads = 0
    var holdReads = 0
    var pageCount = 1
    var failNextPage: Int? = null
    val requestedPages = mutableListOf<Int>()
    val requestedStatuses = mutableListOf<HoldStatus?>()
    var product = ManagedProduct(7, "복숭아 4입", "", 10000, 4000, 10, 5, 2, 3, 2, 0, now + 600000, emptySet(), true, 0)
    var detail = HoldDetail(
        1, "방문손님", "청과 마을", HoldStatus.HOLDING, now - 60000, now + 600000, now, null, 12000,
        listOf(HoldItem(8, 7, "복숭아 4입", 1, 4000, 4000), HoldItem(9, 6, "사과", 2, 4000, 8000)),
    )
    var candidates = HoldCancellations(
        listOf(
            CancellationProduct(
                7,
                "복숭아 4입",
                1,
                listOf(CancellationCandidate(8, 3, now, "방문손님", 1, true), CancellationCandidate(9, 1, now, "배정손님", 2, false)),
            ),
        ),
        "서버에서 내려온 안내 메시지",
        1,
    )
    override fun fetchProduct(id: Long) = flow {
        productReads++
        productReadGate?.await()
        emit(if (failed) Result.failure(IllegalStateException()) else Result.success(product))
    }
    override fun updateStock(id: Long, quantity: Int) = flow {
        writes++
        if (failed) {
            emit(Result.failure(IllegalStateException()))
        } else {
            product = product.copy(stockQuantity = quantity, shortfallQuantity = (3 - quantity).coerceAtLeast(0), availableQuantity = (quantity - 3).coerceAtLeast(0))
            emit(Result.success(product))
        }
    }
    override fun fetchHolds(page: Int, status: HoldStatus?) = flow {
        holdReads++
        requestedStatuses += status
        requestedPages += page
        if (page == failNextPage) {
            failNextPage = null
            emit(Result.failure(IllegalStateException("append failed")))
            return@flow
        }
        if (pageCount > 1) {
            val rows = (1..100).map { index ->
                val id = page * 100L + index
                ManagedHold(id, id, 7, "복숭아", "방문손님$id", 1, detail.heldAt, detail.expiresAt, detail.status)
            }
            emit(Result.success(HoldPage(rows, pageCount * 100L, page == pageCount - 1, System.currentTimeMillis())))
            return@flow
        }
        emit(Result.success(HoldPage(listOf(ManagedHold(8, 1, 7, "복숭아 4입", "방문손님", 1, detail.heldAt, detail.expiresAt, detail.status)).filter { status == null || it.status == status }, 1, true, System.currentTimeMillis())))
    }
    override fun fetchHold(id: Long) = flow {
        detailReads++
        emit(Result.success(detail.copy(serverTime = System.currentTimeMillis())))
    }
    override fun markAsPickedUp(id: Long) = flow {
        writes++
        if (failed) {
            emit(Result.failure(IllegalStateException()))
            return@flow
        }
        detail = detail.copy(status = HoldStatus.COMPLETED, completedAt = System.currentTimeMillis(), serverTime = System.currentTimeMillis())
        emit(Result.success(detail))
    }
    override fun fetchCancellations() = flow {
        cancellationReads++
        emit(Result.success(candidates))
    }
    override fun cancelHolds(ids: Set<Long>) = flow {
        writes++
        candidates = candidates.copy(products = emptyList(), suggestedCount = 0)
        emit(Result.success(candidates))
    }
}
