package com.swyp.mangro.data.owner.home.impl

import com.swyp.mangro.data.owner.home.model.OwnerHome
import com.swyp.mangro.data.owner.home.model.OwnerHomeProduct
import com.swyp.mangro.data.owner.home.model.OwnerHomeVisit
import com.swyp.mangro.data.owner.home.repository.OwnerHomeRepository
import com.swyp.mangro.remote.owner.service.HoldService
import com.swyp.mangro.remote.owner.service.HomeService
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

internal class OwnerHomeRepositoryImpl @Inject constructor(
    private val homeService: HomeService,
    private val holdService: HoldService,
) : OwnerHomeRepository {
    override fun fetchHome(): Flow<Result<OwnerHome>> = request {
        val response = homeService.fetchOwnerHome()
        if (!response.isSuccessful) throw HttpException(response)
        val body = requireNotNull(response.body())
        require(body.store.id > 0)
        OwnerHome(
            storeId = body.store.id,
            storeStatus = body.store.status,
            hasRegisteredProduct = body.hasRegisteredProduct,
            upcomingVisitCount = body.summary.upcomingVisitCount,
            completedTodayCount = body.summary.completedTodayCount,
            onSaleQty = body.summary.onSaleQty,
            unreadNotificationCount = body.unreadNotificationCount,
            expiredTodayCount = body.issues.expiredTodayCount,
            productsShortOfStock = body.issues.productsShortOfStock,
            shortfallQty = body.issues.shortfallQty,
            reconfirmPendingCount = body.reconfirmPendingCount,
            upcomingVisits = body.upcomingVisits.map {
                require(it.holdId > 0)
                OwnerHomeVisit(it.holdId, it.nickname, it.summary, it.totalQty, OffsetDateTime.parse(it.expiresAt).toInstant().toEpochMilli())
            },
            products = body.products.map {
                require(it.id > 0)
                OwnerHomeProduct(it.id, it.name, it.photoUrl, it.salePrice, it.availableQty, it.activeHoldQty, it.shortfallQty, it.category, it.status, it.reconfirmPending)
            },
        )
    }

    override fun markAsPickedUp(holdId: Long): Flow<Result<Unit>> = request {
        require(holdId > 0)
        val response = holdService.completePickup(holdId)
        if (!response.isSuccessful) throw HttpException(response)
        require(requireNotNull(response.body()).status.value == "COMPLETED")
    }
}

private fun <T> request(block: suspend () -> T): Flow<Result<T>> = flow {
    val result = try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
    emit(result)
}.flowOn(Dispatchers.IO)
