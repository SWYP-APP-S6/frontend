package com.swyp.mangro.data.owner.store.impl

import com.swyp.mangro.data.owner.store.mapper.toOwnerStore
import com.swyp.mangro.data.owner.store.mapper.toRequest
import com.swyp.mangro.data.owner.store.model.OwnerStore
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.data.owner.store.model.StoreRegistration
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.remote.owner.service.StoreService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

internal class StoreRepositoryImpl @Inject constructor(private val storeService: StoreService) : StoreRepository {

    @Deprecated("Legacy Method, need to be delete it")
    override fun fetchMyStore(): Flow<Result<OwnerStore>> = flow {
        val result = try {
            val response = storeService.fetchMyStore()
            if (!response.isSuccessful) throw HttpException(response)
            val body = requireNotNull(response.body())
            require(body.id > 0 && body.name.isNotBlank())
            Result.success(
                OwnerStore(
                    body.id,
                    body.name,
                    body.categories.map { it.value },
                    StoreApprovalStatus.from(body.status),
                    body.businessOpenTime,
                    body.businessCloseTime,
                    body.phone,
                ),
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }

        emit(result)
    }.flowOn(Dispatchers.IO)

    override fun fetchMyStoreInformation(): Flow<OwnerStore> = flow {
        val response = storeService.fetchMyStore()
        if (!response.isSuccessful) throw HttpException(response)

        val body = response.body() ?: throw IllegalStateException("Body is null")

        if (body.id <= 0) throw IllegalStateException("Id can't be zero")
        if (body.name.isBlank()) throw IllegalStateException("Name can't be empty")

        val result = body.toOwnerStore()
        emit(result)
    }

    override fun register(registration: StoreRegistration): Flow<Result<Unit>> = flow {
        val result = try {
            val response = storeService.registerStore(registration.toRequest())
            if (!response.isSuccessful) throw HttpException(response)
            require(requireNotNull(response.body()).id > 0)
            Result.success(Unit)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
        emit(result)
    }.flowOn(Dispatchers.IO)
}
