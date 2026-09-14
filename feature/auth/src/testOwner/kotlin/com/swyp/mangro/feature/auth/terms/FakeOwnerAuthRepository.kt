package com.swyp.mangro.feature.auth.terms

import com.swyp.mangro.data.owner.auth.AuthResult
import com.swyp.mangro.data.owner.auth.OwnerAuthRepository
import com.swyp.mangro.data.owner.auth.OwnerSession
import com.swyp.mangro.data.owner.terms.model.OwnerTerm
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeOwnerAuthRepository : OwnerAuthRepository {
    override val session = MutableStateFlow(OwnerSession.SIGNUP_REQUIRED)
    var signupResult: AuthResult<Unit> = AuthResult.Success(Unit)
    var loginResult: AuthResult<OwnerSession> = AuthResult.Success(OwnerSession.SIGNUP_REQUIRED)
    var signupCalls = 0
    override suspend fun login(kakaoAccessToken: String) = loginResult
    override suspend fun signup(documents: List<OwnerTerm>, selectedKeys: Set<String>): AuthResult<Unit> {
        signupCalls++
        return signupResult
    }
    override suspend fun restore() = AuthResult.Success(session.value)
    override suspend fun logout() = AuthResult.Success(Unit)
}
