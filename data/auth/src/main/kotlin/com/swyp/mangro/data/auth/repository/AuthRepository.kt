package com.swyp.mangro.data.auth.repository

import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import kotlinx.coroutines.flow.Flow

/** 화면에서 로그인/가입 요청을 하나씩 실행한다. 가입 토큰은 프로세스 메모리에만 보관한다. */
interface AuthRepository {
    fun logout(): Flow<AuthResult<Unit>>

    fun hasSession(): Flow<Boolean>

    /** 수집할 때마다 검증 API를 한 번 호출하고 토큰 저장 후 결과를 한 번 내보낸다. */
    fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>>

    /** 수집할 때 가입 API를 호출한다. 가입용 토큰은 성공 후 소모되므로 자동 재수집하지 않는다. */
    fun signup(consents: SignupConsents): Flow<AuthResult<Unit>>

    fun guestLogin(): Flow<AuthResult<Unit>>

    fun isGuestSession(): Flow<Boolean>

    fun clearSession(): Flow<AuthResult<Unit>>
}
