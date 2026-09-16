package com.swyp.mangro.remote.auth.service

import com.swyp.mangro.remote.auth.model.RefreshUserAuthKeyRequest
import com.swyp.mangro.remote.auth.model.RefreshUserAuthKeyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * OpenAPI의 POST /auth/refresh 계약입니다. 요청·응답은 생성된 DTO를 그대로 사용합니다.
 * 전역 bearerAuth에 따라 body의 refreshToken과 같은 시점의 accessToken을 명시적으로 전달합니다.
 */
interface TokenRefreshService {
    @POST("auth/refresh")
    suspend fun refresh(
        @Header("Authorization") authorization: String,
        @Body request: RefreshUserAuthKeyRequest,
    ): Response<RefreshUserAuthKeyResponse>
}
