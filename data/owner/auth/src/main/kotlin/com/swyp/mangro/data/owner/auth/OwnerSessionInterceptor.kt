package com.swyp.mangro.data.owner.auth

import com.swyp.mangro.core.network.NetworkClient
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/** Only attach Mangro credentials to its API origin. Public auth/terms use separate clients. */
internal class OwnerSessionInterceptor(
    private val repository: RemoteOwnerAuthRepository,
    private val origin: HttpUrl = NetworkClient.BASE_URL.toHttpUrl(),
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.scheme != origin.scheme || request.url.host != origin.host || request.url.port != origin.port || request.header("Authorization") != null) {
            return chain.proceed(request)
        }
        val token = repository.accessToken() ?: return chain.proceed(request)
        val authenticated = request.newBuilder().header("Authorization", "Bearer $token").build()
        val response = chain.proceed(authenticated)
        if (response.code != 401 || request.body?.isOneShot() == true || request.body?.isDuplex() == true) return response
        val refreshed = runBlocking { repository.refresh(token) }
        if (refreshed !is AuthResult.Success) return response
        response.close()
        return chain.proceed(request.newBuilder().header("Authorization", "Bearer ${refreshed.value}").build())
    }
}
