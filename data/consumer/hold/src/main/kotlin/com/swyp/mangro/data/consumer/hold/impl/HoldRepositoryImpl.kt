package com.swyp.mangro.data.consumer.hold.impl

import com.swyp.mangro.data.consumer.hold.model.HoldDetail
import com.swyp.mangro.data.consumer.hold.model.HoldHistory
import com.swyp.mangro.data.consumer.hold.model.HoldItem
import com.swyp.mangro.data.consumer.hold.model.HoldStore
import com.swyp.mangro.data.consumer.hold.model.HoldSummary
import com.swyp.mangro.data.consumer.hold.repository.HoldRepository
import com.swyp.mangro.remote.consumer.model.HoldDetailResponse
import com.swyp.mangro.remote.consumer.model.HoldItemResponse
import com.swyp.mangro.remote.consumer.model.HoldSummaryResponse
import com.swyp.mangro.remote.consumer.service.HoldService
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

internal class HoldRepositoryImpl @Inject constructor(
    private val holdService: HoldService,
) : HoldRepository {

    override fun fetchHolds(page: Int, size: Int): Flow<Result<HoldHistory>> = request {
        val response = holdService.fetchHolds(page, size)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw HttpException(response)
        }
        val body = requireNotNull(response.body())
        HoldHistory(
            holds = body.holds.content.map { it.toDomain() },
            totalPages = body.holds.totalPages,
            last = body.holds.last,
        )
    }

    override fun fetchHold(holdId: Long): Flow<Result<HoldDetail>> = request {
        val response = holdService.fetchHold(holdId)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw HttpException(response)
        }
        requireNotNull(response.body()).toDomain()
    }

    override fun cancelHold(holdId: Long): Flow<Result<HoldDetail>> = request {
        val response = holdService.cancelHold(holdId)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw HttpException(response)
        }
        requireNotNull(response.body()).toDomain()
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
}

private fun HoldSummaryResponse.toDomain(): HoldSummary = HoldSummary(
    id = id,
    status = status.value,
    storeId = storeId,
    storeName = storeName,
    productId = productId,
    productName = productName,
    photoUrl = photoUrl,
    qty = qty,
    totalPrice = totalPrice,
    heldAtMillis = heldAt.toMillisOrZero(),
    expiresAtMillis = expiresAt.toMillisOrZero(),
    completedAtMillis = completedAt?.toMillisOrNull(),
    canceledAtMillis = canceledAt?.toMillisOrNull(),
)

private fun HoldDetailResponse.toDomain(): HoldDetail = HoldDetail(
    id = id,
    status = status.value,
    totalQty = totalQty,
    totalPrice = totalPrice,
    heldAtMillis = heldAt.toMillisOrZero(),
    expiresAtMillis = expiresAt.toMillisOrZero(),
    completedAtMillis = completedAt?.toMillisOrNull(),
    store = HoldStore(
        id = store.id,
        name = store.name,
        address = store.address,
        addressDetail = store.addressDetail,
        phone = store.phone,
        latitude = store.latitude,
        longitude = store.longitude,
        businessOpenTime = store.businessOpenTime,
        businessCloseTime = store.businessCloseTime,
        openNow = store.openNow,
    ),
    items = items.map { it.toDomain() },
)

private fun HoldItemResponse.toDomain(): HoldItem = HoldItem(
    holdId = holdId,
    productId = productId,
    name = name,
    photoUrl = photoUrl,
    originalPrice = originalPrice,
    salePrice = salePrice,
    discountRate = discountRate.takeIf { it > 0 },
    status = status.value,
    qty = qty,
    lineTotal = lineTotal,
)

private fun String.toMillisOrZero(): Long = toMillisOrNull() ?: 0L

private fun String.toMillisOrNull(): Long? = takeIf { it.isNotBlank() }?.let { OffsetDateTime.parse(it).toInstant().toEpochMilli() }
