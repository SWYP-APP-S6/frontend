package com.swyp.mangro.core.network

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import retrofit2.Response

/** Retains the server envelope without making core depend on a generated role-specific DTO. */
data class RemoteHttpError(
    val httpStatus: Int,
    val body: JsonObject?,
    val retryAfter: String?,
) {
    val code: String? get() = (body?.get("code") as? JsonPrimitive)?.contentOrNull
}

fun Response<*>.readHttpError(): RemoteHttpError? {
    if (isSuccessful) return null
    val body = errorBody()?.use { responseBody ->
        try {
            NetworkClient.json.parseToJsonElement(responseBody.string()) as? JsonObject
        } catch (_: SerializationException) {
            null
        }
    }
    return RemoteHttpError(code(), body, headers()["Retry-After"])
}
