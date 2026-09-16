package com.swyp.mangro.core.network.exception

import com.swyp.mangro.core.network.error.RemoteHttpError
import java.io.IOException

/** 갱신 API 실패의 HTTP 상태, 오류 envelope, Retry-After를 보존합니다. */
class TokenRefreshException(
    val error: RemoteHttpError,
) : IOException("Token refresh failed with HTTP ${error.httpStatus}")
