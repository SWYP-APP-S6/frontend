package com.swyp.mangro.data.auth.impl

import com.swyp.core.local.model.AuthKey
import com.swyp.core.local.store.AuthStore
import com.swyp.mangro.data.auth.BuildConfig
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.remote.auth.model.RegisterUserRequest
import com.swyp.mangro.remote.auth.model.VerifyConsumerKakaoTokenAndLoginRequest
import com.swyp.mangro.remote.auth.model.VerifyOwnerKakaoTokenAndLoginRequest
import com.swyp.mangro.remote.auth.service.AuthService
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class AuthRepositoryImpl @Inject constructor(
    @param:Named("login") private val service: AuthService,
    private val store: AuthStore,
) : AuthRepository {
    private var signupToken: String? = null
    private var pendingSignupKeys: AuthKey? = null

    override fun login(kakaoAccessToken: String): Flow<AuthResult<LoginStatus>> = flow {
        signupToken = null
        pendingSignupKeys = null

        val result = authRequest {
            require(kakaoAccessToken.isNotBlank())
            val response = if (BuildConfig.IS_OWNER) {
                service.verifyOwnerKakaoTokenAndLogin(VerifyOwnerKakaoTokenAndLoginRequest(kakaoAccessToken)).checked()
            } else {
                service.verifyConsumerKakaoTokenAndLogin(VerifyConsumerKakaoTokenAndLoginRequest(kakaoAccessToken)).checked()
            }

            if (response.accessToken != null) {
                val accessToken = requireNotNull(response.accessToken)
                val refreshToken = requireNotNull(response.refreshToken)
                val keys = AuthKey(accessToken = accessToken, refreshToken = refreshToken)

                storage { store.save(keys) }

                LoginStatus.AUTHENTICATED
            } else {
                val token = requireNotNull(response.signupToken)
                require(token.isNotBlank())

                storage { store.clear() }
                signupToken = token

                LoginStatus.SIGNUP_REQUIRED
            }
        }

        emit(result)
    }.flowOn(Dispatchers.IO)

    override fun signup(consents: SignupConsents): Flow<AuthResult<Unit>> = flow {
        val result = authRequest {
            val token = signupToken ?: throw AuthException(AuthFailure.SIGNUP_REQUIRED)
            require(consents.service && consents.privacy)

            if (!BuildConfig.IS_OWNER) require(consents.location && consents.thirdParty)

            val keys = pendingSignupKeys ?: run {
                val response = service.registerUser(
                    RegisterUserRequest(
                        signupToken = token,
                        serviceTermsAgreed = consents.service,
                        privacyTermsAgreed = consents.privacy,
                        locationTermsAgreed = BuildConfig.IS_OWNER || consents.location,
                        thirdPartyTermsAgreed = BuildConfig.IS_OWNER || consents.thirdParty,
                        marketingOptIn = consents.marketing,
                    ),
                )

                if (response.code() == 401) {
                    response.errorBody()?.close()
                    throw AuthException(AuthFailure.SIGNUP_REQUIRED)
                }
                val tokens = response.checked()
                AuthKey(tokens.accessToken, tokens.refreshToken).also { pendingSignupKeys = it }
            }
            storage { store.save(keys) }
            pendingSignupKeys = null
            signupToken = null
        }
        emit(result)
    }.flowOn(Dispatchers.IO)

    override fun hasSession(): Flow<Boolean> = flow {
        val hasSession = try {
            store.authKey.first() != null
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            storage { store.clear() }
            false
        }
        emit(hasSession)
    }.flowOn(Dispatchers.IO)

    private suspend fun <T> storage(block: suspend () -> T): T = try {
        block()
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        throw AuthException(AuthFailure.STORAGE)
    }
}
