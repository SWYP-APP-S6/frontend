package com.swyp.mangro.core.network.interceptor

import com.swyp.mangro.core.network.Constants
import com.swyp.mangro.core.network.util.hasSameOriginAs
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/** API 요청에 현재 회원 토큰을 부착하며 호출자가 명시한 인증 헤더는 유지합니다. */
class AuthorizationInterceptor(
    private val baseUrl: HttpUrl = Constants.BASE_URL.toHttpUrl(),
    private val accessToken: () -> String?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (!request.url.hasSameOriginAs(baseUrl) || request.header("Authorization") != null) {
            return chain.proceed(request)
        }

        val token = accessToken()?.takeIf { it.isNotBlank() }

        return chain.proceed(
            if (token != null && request.header("Authorization") == null) {
                request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                request
            },
        )
    }
}
