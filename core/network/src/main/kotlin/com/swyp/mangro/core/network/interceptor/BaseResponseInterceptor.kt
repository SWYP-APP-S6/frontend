package com.swyp.mangro.core.network.interceptor

import com.swyp.mangro.core.network.exception.BaseResponseException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Invocation

class BaseResponseInterceptor(private val json: Json) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val annotation = request.tag(Invocation::class.java)?.method()?.getAnnotation(UnwrapBaseResponse::class.java)
        val response = chain.proceed(request)
        if (annotation == null || !response.isSuccessful || response.code == 204 || response.code == 205) return response
        val body = response.body ?: throw BaseResponseException(response.code, null, null)
        val envelope = body.use {
            try {
                json.parseToJsonElement(it.string()) as? JsonObject
            } catch (_: SerializationException) {
                null
            }
        } ?: throw BaseResponseException(response.code, null, null)
        val status = (envelope["status"] as? JsonPrimitive)?.takeUnless { it.isString }?.intOrNull
        val code = (envelope["code"] as? JsonPrimitive)?.takeIf { it.isString }?.content
        val data = envelope["data"]
        val successfulCode = code == "OK" || (status == 201 && code == "CREATED")
        if (status == null || status !in 200..299 || !successfulCode || data == null || (data == JsonNull && !annotation.allowNullData)) {
            throw BaseResponseException(response.code, status, code)
        }
        return response.newBuilder().removeHeader("Content-Length").removeHeader("Content-Encoding")
            .body(data.toString().toResponseBody(body.contentType())).build()
    }
}
