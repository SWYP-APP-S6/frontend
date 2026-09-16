package com.swyp.mangro.core.network.provider

/** OkHttp 작업 스레드에서 호출하는 동기 토큰 접근 계약입니다. */
interface TokenProvider {
    /** 현재 accessToken을 조회합니다. 저장소 오류는 IOException으로 전달합니다. */
    fun accessToken(): String?

    /**
     * 실패한 토큰을 갱신하고 저장 완료된 토큰을 반환합니다. 세션이 없거나 변경되면 null입니다.
     * HTTP 실패는 [com.swyp.mangro.core.network.exception.TokenRefreshException], 통신·저장·응답 형식 실패는 IOException으로 전달합니다.
     */
    fun refreshAccessToken(failedAccessToken: String): String?
}
