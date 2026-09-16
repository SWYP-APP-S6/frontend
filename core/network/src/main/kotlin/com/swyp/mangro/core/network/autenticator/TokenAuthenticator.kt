package com.swyp.mangro.core.network

import com.swyp.mangro.core.network.provider.TokenProvider
import com.swyp.mangro.core.network.util.hasSameOriginAs
import java.io.IOException
import okhttp3.Authenticator
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * API 요청이 401 응답을 받으면 토큰 갱신을 시도하고, 새 토큰으로 한 번 재요청합니다.
 *
 * 갱신할 수 없거나 재요청도 401을 받으면 null을 반환해 현재 응답을 호출자에게 전달합니다.
 * null 반환은 저장된 토큰을 삭제하지 않습니다. 로그아웃 시 저장소 정리는 Repository가 담당합니다.
 */
class TokenAuthenticator(
    private val tokenProvider: TokenProvider,
    private val baseUrl: HttpUrl = Constants.BASE_URL.toHttpUrl(),
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        // 이전 401로 이미 재요청했다면 종료합니다.
        if (generateSequence(response.priorResponse) { it.priorResponse }.any { it.code == 401 }) return null

        val request = response.request

        // 다른 서버로 이동한 요청에 저장된 인증 토큰을 보내지 않습니다.
        if (!request.url.hasSameOriginAs(baseUrl)) return null

        // 실제 실패한 요청의 토큰을 전달해야 Provider가 현재 저장된 세션과 비교할 수 있습니다.
        // 인증 헤더가 없거나 Bearer 토큰을 읽을 수 없으면 갱신을 시도하지 않습니다.
        val authorization = request.header("Authorization") ?: return null

        if (!authorization.startsWith("Bearer ", ignoreCase = true)) return null
        val failedToken = authorization.substring(7).trim().takeIf { it.isNotEmpty() } ?: return null

        // Provider가 갱신 및 저장을 처리합니다. 갱신 실패는 재전송 없이 현재 401 응답으로 종료합니다.
        // null·빈 값·기존과 같은 토큰도 재요청에 사용하지 않습니다.
        val token = try {
            tokenProvider.refreshAccessToken(failedToken)
        } catch (_: IOException) {
            null
        }?.takeIf {
            it.isNotBlank() && it != failedToken
        } ?: return null

        // 기존 요청의 나머지 내용은 유지하고 Authorization만 새 토큰으로 교체합니다.
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}
