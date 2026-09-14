package com.swyp.mangro.data.owner.auth

import com.swyp.mangro.data.owner.terms.model.OwnerTerm
import kotlinx.coroutines.flow.StateFlow

interface OwnerAuthRepository {
    val session: StateFlow<OwnerSession>
    suspend fun login(kakaoAccessToken: String): AuthResult<OwnerSession>
    suspend fun signup(documents: List<OwnerTerm>, selectedKeys: Set<String>): AuthResult<Unit>
    suspend fun restore(): AuthResult<OwnerSession>
    suspend fun logout(): AuthResult<Unit>
}

enum class OwnerSession { SIGNED_OUT, SIGNUP_REQUIRED, AUTHENTICATED }
enum class AuthFailure { NETWORK, UNAUTHORIZED, INVALID_RESPONSE, SERVER, STORAGE, CONSENT_CHANGED }
sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Failure(val reason: AuthFailure) : AuthResult<Nothing>
}
