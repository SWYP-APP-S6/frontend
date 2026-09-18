package com.swyp.mangro.data.consumer.notification

import com.swyp.mangro.remote.user.model.DeleteDeviceTokenRequest
import com.swyp.mangro.remote.user.model.RegisterDeviceTokenRequest
import com.swyp.mangro.remote.user.service.NotificationService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

class ConsumerNotificationRepository @Inject constructor(private val service: NotificationService) {
    fun markAsRead(notificationId: Long): Flow<Result<Unit>> = request {
        require(notificationId > 0)
        val response = service.readNotification(notificationId)
        if (!response.isSuccessful) throw HttpException(response)
    }
    fun registerToken(token: String): Flow<Result<Unit>> = request {
        require(token.isNotBlank())
        val response = service.registerDeviceToken(RegisterDeviceTokenRequest(token, RegisterDeviceTokenRequest.Platform.ANDROID))
        if (!response.isSuccessful) throw HttpException(response)
    }
    fun deleteToken(token: String): Flow<Result<Unit>> = request {
        require(token.isNotBlank())
        val response = service.deleteDeviceToken(DeleteDeviceTokenRequest(token))
        if (!response.isSuccessful) throw HttpException(response)
    }
    private fun request(block: suspend () -> Unit): Flow<Result<Unit>> = flow {
        val result = try {
            block()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }.flowOn(Dispatchers.IO)
}
