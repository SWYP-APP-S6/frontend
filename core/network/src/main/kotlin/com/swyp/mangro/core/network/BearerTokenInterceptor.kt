package com.swyp.mangro.core.network

import okhttp3.Interceptor
import okhttp3.Response

/** Supply this only to authenticated clients. Token storage and refresh belong to the caller. */
class BearerTokenInterceptor(private val accessToken: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = accessToken()?.takeIf { it.isNotBlank() }
        return chain.proceed(
            if (token != null && request.header("Authorization") == null) {
                request.newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                request
            },
        )
    }
}
