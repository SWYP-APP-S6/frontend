package com.swyp.mangro.data.user.impl

import com.swyp.mangro.data.user.model.UserProfile
import com.swyp.mangro.data.user.repository.UserRepository
import com.swyp.mangro.remote.user.service.UserService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

internal class UserRepositoryImpl @Inject constructor(private val service: UserService) : UserRepository {
    override fun fetchMe(): Flow<Result<UserProfile>> = flow {
        val result = try {
            val response = service.fetchMe()
            if (!response.isSuccessful) throw HttpException(response)
            val body = requireNotNull(response.body())
            require(body.id > 0)
            Result.success(UserProfile(body.id, body.role.value, body.nickname, body.phone, body.marketingOptIn, body.termsAgreedAt, body.joinedAt))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
        emit(result)
    }.flowOn(Dispatchers.IO)
}
