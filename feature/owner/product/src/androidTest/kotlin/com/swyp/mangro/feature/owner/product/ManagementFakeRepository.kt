package com.swyp.mangro.feature.owner.product

import com.swyp.mangro.data.owner.product.model.CancellationCandidate
import com.swyp.mangro.data.owner.product.model.CancellationProduct
import com.swyp.mangro.data.owner.product.model.HoldCancellations
import com.swyp.mangro.data.owner.product.model.HoldDetail
import com.swyp.mangro.data.owner.product.model.HoldItem
import com.swyp.mangro.data.owner.product.model.HoldPage
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedHold
import com.swyp.mangro.data.owner.product.model.ManagedProduct
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class ManagementFakeRepository : OwnerProductRepository {
    private val now = System.currentTimeMillis()
    var failed = false
    var writes = 0
    var cancellationReads = 0
    var holdReads = 0
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
    override fun fetchProduct(id: Long) = flowOf(if (failed) Result.failure(IllegalStateException()) else Result.success(product))
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
        emit(Result.success(HoldPage(listOf(ManagedHold(8, 1, 7, "복숭아 4입", "방문손님", 1, detail.heldAt, detail.expiresAt, detail.status)).filter { status == null || it.status == status }, 1, true, System.currentTimeMillis())))
    }
    override fun fetchHold(id: Long) = flowOf(Result.success(detail.copy(serverTime = System.currentTimeMillis())))
    override fun markAsPickedUp(id: Long) = flow {
        writes++
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
