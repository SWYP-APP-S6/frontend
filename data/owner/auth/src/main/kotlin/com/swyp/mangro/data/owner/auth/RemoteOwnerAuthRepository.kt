package com.swyp.mangro.data.owner.auth

import com.swyp.mangro.data.owner.auth.storage.OwnerTokenStore
import com.swyp.mangro.data.owner.auth.storage.OwnerTokens
import com.swyp.mangro.data.owner.terms.model.OwnerTerm
import com.swyp.mangro.data.owner.terms.model.TermsFailure
import com.swyp.mangro.data.owner.terms.model.TermsResult
import com.swyp.mangro.data.owner.terms.repository.OwnerTermsRepository
import com.swyp.mangro.remote.auth.model.LogoutRequest
import com.swyp.mangro.remote.auth.model.RefreshUserAuthKeyRequest
import com.swyp.mangro.remote.auth.model.TokenResponse
import com.swyp.mangro.remote.auth.model.VerifyOwnerKakaoTokenAndLoginRequest
import com.swyp.mangro.remote.auth.service.AuthService
import java.io.EOFException
import java.io.IOException
import java.security.GeneralSecurityException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.Response

internal class RemoteOwnerAuthRepository(
    private val service: AuthService,
    private val store: OwnerTokenStore,
    private val terms: OwnerTermsRepository,
) : OwnerAuthRepository {
    private val mutex = Mutex()
    private val mutableSession = MutableStateFlow(OwnerSession.SIGNED_OUT)
    override val session = mutableSession.asStateFlow()

    @Volatile private var tokens: OwnerTokens? = null
    private var signupToken: String? = null

    override suspend fun login(kakaoAccessToken: String): AuthResult<OwnerSession> = operation {
        require(kakaoAccessToken.isNotBlank())
        val body = service.verifyOwnerKakaoTokenAndLogin(VerifyOwnerKakaoTokenAndLoginRequest(kakaoAccessToken)).checked()
        validateEnvelope(body.status, body.code)
        if (body.data.registered) {
            save(TokenResponse(body.data.accessToken, body.data.refreshToken))
        } else {
            require(body.data.signupToken.isNotBlank())
            store.clear()
            tokens = null
            signupToken = body.data.signupToken
            mutableSession.value = OwnerSession.SIGNUP_REQUIRED
        }
        mutableSession.value
    }

    override suspend fun signup(documents: List<OwnerTerm>, selectedKeys: Set<String>): AuthResult<Unit> = operation {
        val pending = signupToken ?: throw AuthException(AuthFailure.UNAUTHORIZED)
        // Recheck versions immediately before submission; never silently accept updated terms.
        val current = when (val result = terms.fetchTerms()) {
            is TermsResult.Success -> result.value
            is TermsResult.Failure -> throw AuthException(if (result.reason == TermsFailure.NETWORK) AuthFailure.NETWORK else AuthFailure.SERVER)
        }
        if (current != documents) throw AuthException(AuthFailure.CONSENT_CHANGED)
        val request = signupRequest(pending, current, selectedKeys)
        val body = service.registerUser(request).checked()
        validateEnvelope(body.status, body.code)
        save(body.data)
    }

    override suspend fun restore(): AuthResult<OwnerSession> = operation {
        if (tokens == null) tokens = readStoredTokens()
        if (tokens != null) refreshLocked()
        mutableSession.value
    }

    internal fun accessToken(): String? = tokens?.accessToken

    /** The rejected token prevents parallel 401s from rotating the same refresh token twice. */
    internal suspend fun refresh(rejectedAccessToken: String): AuthResult<String> = operation {
        val current = tokens ?: throw AuthException(AuthFailure.UNAUTHORIZED)
        if (current.accessToken == rejectedAccessToken) refreshLocked()
        tokens?.accessToken ?: throw AuthException(AuthFailure.UNAUTHORIZED)
    }

    override suspend fun logout(): AuthResult<Unit> = operation {
        val current = tokens ?: readStoredTokens()
        try {
            if (current != null) {
                val body = service.logout(LogoutRequest(current.refreshToken)).checked()
                validateEnvelope(body.status, body.code)
            }
        } finally {
            clearSession()
        }
    }

    private fun readStoredTokens(): OwnerTokens? = try {
        store.read()
    } catch (error: GeneralSecurityException) {
        clearSession()
        null
    } catch (error: IllegalArgumentException) {
        clearSession()
        null
    } catch (error: EOFException) {
        clearSession()
        null
    } catch (error: IOException) {
        throw AuthException(AuthFailure.STORAGE)
    }

    private suspend fun refreshLocked() {
        val current = tokens ?: throw AuthException(AuthFailure.UNAUTHORIZED)
        val response = service.refreshUserAuthKey(RefreshUserAuthKeyRequest(current.refreshToken))
        if (response.code() == 401) clearSession()
        val body = response.checked()
        validateEnvelope(body.status, body.code)
        save(body.data)
    }

    private fun save(value: TokenResponse) {
        require(value.accessToken.isNotBlank() && value.refreshToken.isNotBlank())
        val updated = OwnerTokens(value.accessToken, value.refreshToken)
        try {
            store.write(updated)
        } catch (error: Exception) {
            throw AuthException(AuthFailure.STORAGE)
        }
        tokens = updated
        signupToken = null
        mutableSession.value = OwnerSession.AUTHENTICATED
    }

    private fun clearSession() {
        tokens = null
        signupToken = null
        mutableSession.value = OwnerSession.SIGNED_OUT
        store.clear()
    }

    private suspend fun <T> operation(block: suspend () -> T): AuthResult<T> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                AuthResult.Success(block())
            } catch (error: CancellationException) {
                throw error
            } catch (error: AuthException) {
                AuthResult.Failure(error.reason)
            } catch (error: GeneralSecurityException) {
                AuthResult.Failure(AuthFailure.STORAGE)
            } catch (error: IOException) {
                AuthResult.Failure(AuthFailure.NETWORK)
            } catch (error: IllegalArgumentException) {
                AuthResult.Failure(AuthFailure.INVALID_RESPONSE)
            }
        }
    }
}

private fun validateEnvelope(status: Int, code: String) {
    require(status == 200 && code == "OK")
}
private fun <T> Response<T>.checked(): T {
    if (!isSuccessful) throw AuthException(if (code() == 401) AuthFailure.UNAUTHORIZED else AuthFailure.SERVER)
    return body() ?: throw AuthException(AuthFailure.INVALID_RESPONSE)
}
internal class AuthException(val reason: AuthFailure) : Exception()
