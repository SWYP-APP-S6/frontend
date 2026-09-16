package com.swyp.mangro.data.auth

import com.swyp.core.local.model.AuthKey
import com.swyp.core.local.model.UserInfo
import com.swyp.core.local.store.AuthStore
import com.swyp.core.local.store.UserInfoStore
import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.core.network.interceptor.AuthorizationInterceptor
import com.swyp.mangro.core.network.interceptor.BaseResponseInterceptor
import com.swyp.mangro.data.auth.impl.AuthRepositoryImpl
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.remote.auth.service.AuthService
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RemoteAuthRepositoryTest {
    private val server = MockWebServer()
    private val store = MemoryStore()
    private val service by lazy {
        NetworkModule.provideRetrofit(okhttp3.OkHttpClient(), NetworkModule.provideNetworkJson()).newBuilder().baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().addInterceptor(BaseResponseInterceptor(Json)).build())
            .build().create(AuthService::class.java)
    }
    private var userCleared = false
    private val userStore = object : UserInfoStore {
        override val userInfo = MutableStateFlow<UserInfo?>(null)
        override suspend fun save(userInfo: UserInfo) {
            this.userInfo.value = userInfo
        }
        override suspend fun clear() {
            userCleared = true
            userInfo.value = null
        }
    }
    private val authenticatedService by lazy {
        val client = OkHttpClient.Builder().addInterceptor(AuthorizationInterceptor(server.url("/")) { store.authKey.value?.accessToken }).build()
        NetworkModule.provideRetrofit(client, NetworkModule.provideNetworkJson()).newBuilder().baseUrl(server.url("/")).build().create(AuthService::class.java)
    }
    private fun repository() = AuthRepositoryImpl(service, store, authenticatedService, userStore)
    private fun enqueue(data: String) {
        server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":$data}"""))
    }
    private val member = """{"registered":true,"accessToken":"app-access","refreshToken":"app-refresh"}"""
    private val newcomer = """{"registered":false,"signupToken":"signup-only"}"""
    private val consents = SignupConsents(true, true, true, true, false)

    @After fun tearDown() = server.shutdown()

    @Test fun logoutSendsRefreshTokenAndClearsSessionAndUser() = runTest {
        store.save(AuthKey("access", "refresh"))
        enqueue("null")
        assertEquals(AuthResult.Success(Unit), repository().logout().single())
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/auth/logout", request.path)
        assertEquals("Bearer access", request.getHeader("Authorization"))
        assertEquals("""{"refreshToken":"refresh"}""", request.body.readUtf8())
        assertNull(store.authKey.value)
        assertTrue(userCleared)
    }

    @Test fun failedLogoutKeepsSessionForRetry() = runTest {
        val keys = AuthKey("access", "refresh")
        store.save(keys)
        server.enqueue(MockResponse().setResponseCode(503))
        assertEquals(AuthResult.Failure(AuthFailure.SERVER), repository().logout().single())
        assertEquals(keys, store.authKey.value)
        assertEquals(false, userCleared)
    }

    @Test fun alreadyExpiredSessionCanLogout() = runTest {
        store.save(AuthKey("access", "refresh"))
        server.enqueue(MockResponse().setResponseCode(401))
        assertEquals(AuthResult.Success(Unit), repository().logout().single())
        assertNull(store.authKey.value)
        assertTrue(userCleared)
    }

    @Test fun buildFlavorUsesItsEndpointAndSendsRawKakaoToken() = runTest {
        enqueue(member)
        assertEquals(AuthResult.Success(LoginStatus.AUTHENTICATED), repository().login("kakao-raw").single())
        val request = server.takeRequest()
        val expectedPath = when (BuildConfig.FLAVOR) {
            "owner" -> "/auth/owner/kakao"
            "consumer" -> "/auth/consumer/kakao"
            else -> error("Unexpected flavor: ${BuildConfig.FLAVOR}")
        }
        assertEquals(expectedPath, request.path)
        assertEquals("POST", request.method)
        assertEquals("""{"kakaoAccessToken":"kakao-raw"}""", request.body.readUtf8())
        assertNull(request.getHeader("Authorization"))
        assertEquals(AuthKey("app-access", "app-refresh"), store.authKey.value)
    }

    @Test fun newMemberStoresNoMemberTokensUntilSignupSucceeds() = runTest {
        val repository = repository()
        store.save(AuthKey("old", "old-refresh"))
        enqueue(newcomer)
        assertEquals(AuthResult.Success(LoginStatus.SIGNUP_REQUIRED), repository.login("kakao").single())
        assertNull(store.authKey.value)
        server.takeRequest()
        enqueue("""{"accessToken":"new","refreshToken":"new-refresh"}""")
        assertEquals(AuthResult.Success(Unit), repository.signup(consents).single())
        val request = server.takeRequest()
        assertEquals("/auth/signup", request.path)
        val body = Json.parseToJsonElement(request.body.readUtf8()).toString()
        assertTrue(body.contains("\"signupToken\":\"signup-only\""))
        assertTrue(body.contains("\"marketingOptIn\":false"))
        assertTrue(body.contains("\"locationTermsAgreed\":true"))
        assertTrue(body.contains("\"thirdPartyTermsAgreed\":true"))
        assertEquals(AuthKey("new", "new-refresh"), store.authKey.value)
        assertEquals(AuthResult.Failure(AuthFailure.SIGNUP_REQUIRED), repository.signup(consents).single())
    }

    @Test fun consumerOnlyConsentsAreRequiredOnlyForConsumerSignup() = runTest {
        val repository = repository()
        enqueue(newcomer)
        repository.login("kakao").single()
        server.takeRequest()
        if (BuildConfig.IS_OWNER) enqueue("""{"accessToken":"new","refreshToken":"new-refresh"}""")
        val result = repository.signup(consents.copy(location = false, thirdParty = false)).single()
        if (BuildConfig.IS_OWNER) {
            assertEquals(AuthResult.Success(Unit), result)
            val body = server.takeRequest().body.readUtf8()
            assertTrue(body.contains("\"locationTermsAgreed\":true"))
            assertTrue(body.contains("\"thirdPartyTermsAgreed\":true"))
        } else {
            assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), result)
            assertEquals(1, server.requestCount)
        }
    }

    @Test fun invalidOAuthTokenNeverCreatesSession() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"code":"INVALID_OAUTH_TOKEN"}"""))
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_OAUTH_TOKEN), repository().login("bad").single())
        assertNull(store.authKey.value)
        assertEquals(1, server.requestCount)
    }

    @Test fun invalidEnvelopeAndMissingSignupTokenFailClosed() = runTest {
        server.enqueue(MockResponse().setBody("""{"status":200,"code":"ERROR","data":$member}"""))
        assertEquals(AuthResult.Failure(AuthFailure.SERVER), repository().login("kakao").single())
        enqueue("""{"registered":false}""")
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository().login("kakao").single())
        assertNull(store.authKey.value)
    }

    @Test fun storageFailureIsNotReportedAsAuthenticated() = runTest {
        store.saveFailure = IOException("disk")
        enqueue(member)
        assertEquals(AuthResult.Failure(AuthFailure.STORAGE), repository().login("kakao").single())
    }

    @Test fun cancellationIsPropagated() = runTest {
        store.saveFailure = CancellationException("cancel")
        enqueue(member)
        try {
            repository().login("kakao").single()
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {
            assertNull(store.authKey.value)
        }
    }

    @Test fun failedSignupCanRetryWithoutLosingPendingToken() = runTest {
        val repository = repository()
        enqueue(newcomer)
        repository.login("kakao").single()
        server.enqueue(MockResponse().setResponseCode(503))
        assertEquals(AuthResult.Failure(AuthFailure.SERVER), repository.signup(consents).single())
        enqueue("""{"accessToken":"new","refreshToken":"new-refresh"}""")
        assertEquals(AuthResult.Success(Unit), repository.signup(consents).single())
    }

    @Test fun newLoginAttemptInvalidatesEarlierSignupToken() = runTest {
        val repository = repository()
        enqueue(newcomer)
        repository.login("first").single()
        server.enqueue(MockResponse().setResponseCode(401))
        repository.login("second").single()
        assertEquals(AuthResult.Failure(AuthFailure.SIGNUP_REQUIRED), repository.signup(consents).single())
    }

    @Test fun loginFlowIsColdAndEmitsOneResultPerCollection() = runTest {
        val login = repository().login("kakao")
        assertEquals(0, server.requestCount)
        enqueue(member)
        assertEquals(listOf(AuthResult.Success(LoginStatus.AUTHENTICATED)), login.toList())
        enqueue(member)
        assertEquals(listOf(AuthResult.Success(LoginStatus.AUTHENTICATED)), login.toList())
        assertEquals(2, server.requestCount)
    }

    @Test fun collectorFailureIsNotConvertedToRepositoryFailure() = runTest {
        enqueue(member)
        val failure = IllegalArgumentException("collector")
        try {
            repository().login("kakao").collect { throw failure }
            fail("Collector exception must propagate")
        } catch (error: IllegalArgumentException) {
            assertEquals(failure.message, error.message)
        }
    }

    @Test fun nullAccessTokenRequiresSignupEvenWhenRegisteredFlagIsTrue() = runTest {
        enqueue("""{"registered":true,"accessToken":null,"refreshToken":null,"signupToken":"signup-only"}""")
        assertEquals(AuthResult.Success(LoginStatus.SIGNUP_REQUIRED), repository().login("sdk-access").single())
        assertNull(store.authKey.value)
    }

    @Test fun signupStoresServerTokensOnlyAfterSuccessfulRegistration() = runTest {
        val repository = repository()
        enqueue("""{"registered":false,"accessToken":null,"refreshToken":null,"signupToken":"signup-only"}""")
        assertEquals(AuthResult.Success(LoginStatus.SIGNUP_REQUIRED), repository.login("sdk-access").single())
        assertNull(store.authKey.value)
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"status":201,"code":"CREATED","data":{"accessToken":"signup-access","refreshToken":"signup-refresh"}}"""))
        assertEquals(AuthResult.Success(Unit), repository.signup(consents).single())
        assertEquals(AuthKey("signup-access", "signup-refresh"), store.authKey.value)
        assertTrue(repository.hasSession().single())
    }

    @Test fun localSaveRetryDoesNotRepeatSuccessfulSignup() = runTest {
        val repository = repository()
        enqueue(newcomer)
        repository.login("sdk-access").single()
        enqueue("""{"accessToken":"signup-access","refreshToken":"signup-refresh"}""")
        store.saveFailure = IOException("disk")
        assertEquals(AuthResult.Failure(AuthFailure.STORAGE), repository.signup(consents).single())
        assertNull(store.authKey.value)
        store.saveFailure = null
        assertEquals(AuthResult.Success(Unit), repository.signup(consents).single())
        assertEquals(2, server.requestCount)
        assertEquals(AuthKey("signup-access", "signup-refresh"), store.authKey.value)
    }

    @Test fun missingSdkAccessTokenNeverCallsServerOrCreatesSession() = runTest {
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository().login("").single())
        assertEquals(0, server.requestCount)
        assertNull(store.authKey.value)
    }

    @Test fun sessionRestorationReadsStoredServerTokensWithoutLoginRequest() = runTest {
        val repository = repository()
        assertEquals(false, repository.hasSession().single())
        store.save(AuthKey("app-access", "app-refresh"))
        assertEquals(true, repository.hasSession().single())
        assertEquals(0, server.requestCount)
    }

    @Test fun ownerAlwaysSendsLocationAndThirdPartyConsentsAsTrue() = runTest {
        org.junit.Assume.assumeTrue(BuildConfig.IS_OWNER)
        val repository = repository()
        enqueue(newcomer)
        repository.login("sdk-access").single()
        enqueue("""{"accessToken":"signup-access","refreshToken":"signup-refresh"}""")
        assertEquals(AuthResult.Success(Unit), repository.signup(SignupConsents(true, true, false, false, false)).single())
        server.takeRequest()
        val body = server.takeRequest().body.readUtf8()
        assertTrue(body.contains("\"locationTermsAgreed\":true"))
        assertTrue(body.contains("\"thirdPartyTermsAgreed\":true"))
        assertTrue(body.contains("\"marketingOptIn\":false"))
    }

    @Test fun expiredSignupTokenRequiresLoginWithoutSavingTokens() = runTest {
        val repository = repository()
        enqueue(newcomer)
        repository.login("sdk-access").single()
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"code":"UNAUTHORIZED"}"""))
        assertEquals(AuthResult.Failure(AuthFailure.SIGNUP_REQUIRED), repository.signup(consents).single())
        assertNull(store.authKey.value)
    }

    @Test fun accessTokenAuthenticatesEvenWhenRegisteredFlagIsAbsent() = runTest {
        enqueue("""{"accessToken":"app-access","refreshToken":"app-refresh"}""")
        assertEquals(AuthResult.Success(LoginStatus.AUTHENTICATED), repository().login("sdk-access").single())
        assertEquals(AuthKey("app-access", "app-refresh"), store.authKey.value)
    }

    @Test fun incompleteLoginTokensNeverCreateSessionOrFallBackToSdkTokens() = runTest {
        for (data in listOf(
            """{"accessToken":"app-access","refreshToken":null}""",
            """{"accessToken":"app-access","refreshToken":""}""",
            """{"accessToken":"","refreshToken":"app-refresh"}""",
            """{"accessToken":null,"refreshToken":null,"signupToken":null}""",
        )) {
            enqueue(data)
            assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository().login("sdk-access").single())
            assertNull(store.authKey.value)
        }
    }

    @Test fun nullOrIncompleteSignupTokensNeverCreateSession() = runTest {
        for (data in listOf(
            "null",
            """{"accessToken":"signup-access"}""",
            """{"accessToken":"","refreshToken":"signup-refresh"}""",
            """{"accessToken":null,"refreshToken":null}""",
        )) {
            val repository = repository()
            enqueue(newcomer)
            repository.login("sdk-access").single()
            server.enqueue(MockResponse().setResponseCode(201).setBody("""{"status":201,"code":"CREATED","data":$data}"""))
            assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository.signup(consents).single())
            assertNull(store.authKey.value)
            assertEquals(false, repository.hasSession().single())
        }
    }

    private class MemoryStore : AuthStore {
        override val authKey = MutableStateFlow<AuthKey?>(null)
        var saveFailure: Exception? = null
        override suspend fun save(authKey: AuthKey) {
            saveFailure?.let { throw it }
            this.authKey.value = authKey
        }
        override suspend fun clear() {
            authKey.value = null
        }
        override suspend fun replaceIfMatches(expected: AuthKey, updated: AuthKey): Boolean {
            if (authKey.value != expected) return false
            authKey.value = updated
            return true
        }
    }
}
