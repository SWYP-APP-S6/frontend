package com.swyp.mangro.core.network.util

import okhttp3.HttpUrl

/** 스킴, 호스트, 포트가 모두 같은 서버 주소인지 확인합니다. */
internal fun HttpUrl.hasSameOriginAs(other: HttpUrl): Boolean = scheme == other.scheme && host == other.host && port == other.port
