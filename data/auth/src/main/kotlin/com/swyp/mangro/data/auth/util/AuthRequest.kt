package com.swyp.mangro.data.auth.util

import com.swyp.mangro.core.network.exception.BaseResponseException
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import retrofit2.Response

internal class AuthException(val reason: AuthFailure) : RuntimeException()

internal fun failureFor(code: String?): AuthFailure = when (code) {
    "INVALID_OAUTH_TOKEN" -> AuthFailure.INVALID_OAUTH_TOKEN
    "INVALID_SIGNUP_TOKEN", "EXPIRED_SIGNUP_TOKEN" -> AuthFailure.SIGNUP_REQUIRED
    else -> AuthFailure.SERVER
}

internal fun <T> Response<T>.checked(): T {
    if (!isSuccessful) {
        val code = errorBody()?.use { body ->
            runCatching { (Json.parseToJsonElement(body.string()).jsonObject["code"] as? JsonPrimitive)?.content }.getOrNull()
        }
        throw AuthException(failureFor(code))
    }
    return requireNotNull(body())
}

internal suspend fun <T> authRequest(block: suspend () -> T): AuthResult<T> = try {
    AuthResult.Success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: AuthException) {
    AuthResult.Failure(error.reason)
} catch (error: BaseResponseException) {
    AuthResult.Failure(if (error.code == null || error.code == "OK" || error.code == "CREATED") AuthFailure.INVALID_RESPONSE else failureFor(error.code))
} catch (_: SerializationException) {
    AuthResult.Failure(AuthFailure.INVALID_RESPONSE)
} catch (_: IllegalArgumentException) {
    AuthResult.Failure(AuthFailure.INVALID_RESPONSE)
} catch (_: IOException) {
    AuthResult.Failure(AuthFailure.NETWORK)
}
