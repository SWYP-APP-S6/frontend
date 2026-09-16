package com.swyp.mangro.data.owner.store.impl

import com.swyp.mangro.data.owner.store.model.StoreRegistration
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.remote.owner.model.RegisterStoreRequest
import com.swyp.mangro.remote.owner.service.StoreService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

internal class StoreRepositoryImpl @Inject constructor(private val service: StoreService) : StoreRepository {
    override fun register(registration: StoreRegistration): Flow<Result<Unit>> = flow {
        val result = try {
            val response = service.registerStore(registration.toRequest())
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

private fun StoreRegistration.toRequest(): RegisterStoreRequest {
    require(name.isNotBlank() && name.length <= 100)
    require(postalCode.matches(Regex("[0-9]{5}")))
    require(address.isNotBlank() && address.length <= 255 && addressDetail.length <= 255)
    require(phone.matches(Regex("0[0-9]{8,10}")))
    require(openingMinutes in 0..1380 && closingMinutes in openingMinutes..1380)
    require(openingMinutes % 60 == 0 && closingMinutes % 60 == 0)
    require(businessDays.isNotEmpty() && businessDays.all { it in 0..6 })
    val days = listOf(
        RegisterStoreRequest.BusinessDays.SUNDAY,
        RegisterStoreRequest.BusinessDays.MONDAY,
        RegisterStoreRequest.BusinessDays.TUESDAY,
        RegisterStoreRequest.BusinessDays.WEDNESDAY,
        RegisterStoreRequest.BusinessDays.THURSDAY,
        RegisterStoreRequest.BusinessDays.FRIDAY,
        RegisterStoreRequest.BusinessDays.SATURDAY,
    )
    fun time(minutes: Int) = "${(minutes / 60).toString().padStart(2, '0')}:${(minutes % 60).toString().padStart(2, '0')}:00"
    return RegisterStoreRequest(
        name = name,
        categories = setOf(RegisterStoreRequest.Categories.valueOf(categoryId)),
        postalCode = postalCode,
        address = address,
        addressDetail = addressDetail,
        phone = phone,
        businessOpenTime = time(openingMinutes),
        businessCloseTime = time(closingMinutes),
        businessDays = businessDays.sorted().map { days[it] }.toSet(),
        businessRegistrationNumber = "",
        applicationNote = "",
    )
}
