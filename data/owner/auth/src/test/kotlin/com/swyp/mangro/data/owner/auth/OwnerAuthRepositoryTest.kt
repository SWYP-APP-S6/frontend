package com.swyp.mangro.data.owner.auth

import com.swyp.mangro.core.network.NetworkClient
import com.swyp.mangro.data.owner.auth.storage.OwnerTokenStore
import com.swyp.mangro.data.owner.auth.storage.OwnerTokens
import com.swyp.mangro.data.owner.terms.model.OwnerTerm
import com.swyp.mangro.data.owner.terms.model.OwnerTermDetail
import com.swyp.mangro.data.owner.terms.model.TermsFailure
import com.swyp.mangro.data.owner.terms.model.TermsRequirement
import com.swyp.mangro.data.owner.terms.model.TermsResult
import com.swyp.mangro.data.owner.terms.repository.OwnerTermsRepository
import com.swyp.mangro.remote.auth.service.AuthService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerAuthRepositoryTest {
    private val server = MockWebServer()
    private val storage = object : OwnerTokenStore {
        var value: OwnerTokens? = null
        var readFailure: Exception? = null
        override fun read(): OwnerTokens? {
            readFailure?.let { throw it }
            return value
        }
        override fun write(tokens: OwnerTokens) {
            value = tokens
        }
        override fun clear() {
            value = null
        }
    }
    private val documents = listOf(
        OwnerTerm(7, "SERVICE", 1, "서비스", TermsRequirement.REQUIRED, "2026-09-16"),
        OwnerTerm(8, "PRIVACY_COLLECTION", 1, "개인정보 수집", TermsRequirement.REQUIRED, "2026-09-16"),
        OwnerTerm(9, "MARKETING", 1, "마케팅", TermsRequirement.OPTIONAL, "2026-09-16"),
        OwnerTerm(10, "PRIVACY_POLICY", 1, "처리방침", TermsRequirement.NOTICE, "2026-09-16"),
    )
    private var current = documents
    private val terms = object : OwnerTermsRepository {
        override suspend fun fetchTerms() = TermsResult.Success(current)
        override suspend fun fetchTerm(id: Long): TermsResult<OwnerTermDetail> = TermsResult.Failure(TermsFailure.NOT_FOUND)
    }
    private val repository = RemoteOwnerAuthRepository(NetworkClient.create(server.url("/").toString()).create(AuthService::class.java), storage, terms)

    @After fun close() {
        server.shutdown()
    }
    private fun response(data: String) = MockResponse().setHeader("Content-Type", "application/json").setBody("""{"status":200,"code":"OK","data":$data}""")
    private fun tokenResponse(access: String = "access", refresh: String = "refresh") = response("""{"accessToken":"$access","refreshToken":"$refresh"}""")
    private suspend fun newLogin() {
        server.enqueue(response("""{"registered":false,"signupToken":"signup"}"""))
        assertEquals(AuthResult.Success(OwnerSession.SIGNUP_REQUIRED), repository.login("kakao"))
        assertEquals("/auth/owner/kakao", server.takeRequest().path)
    }
    private suspend fun existingLogin() {
        server.enqueue(response("""{"registered":true,"accessToken":"access","refreshToken":"refresh"}"""))
        assertEquals(AuthResult.Success(OwnerSession.AUTHENTICATED), repository.login("kakao"))
        server.takeRequest()
    }

    @Test fun newMemberHasNoAppSessionUntilSignupAndOnlySelectedConsentsAreSent() = runBlocking {
        newLogin()
        assertNull(storage.value)
        server.enqueue(tokenResponse())
        assertEquals(AuthResult.Success(Unit), repository.signup(documents, setOf("7:1", "8:1", "10:1")))
        val request = server.takeRequest()
        assertEquals("/auth/signup", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"serviceTermsAgreed\":true"))
        assertTrue(body.contains("\"privacyTermsAgreed\":true"))
        assertTrue(body.contains("\"marketingOptIn\":false"))
        assertTrue(body.contains("\"locationTermsAgreed\":false"))
        assertTrue(body.contains("\"thirdPartyTermsAgreed\":false"))
        assertEquals(OwnerSession.AUTHENTICATED, repository.session.value)
    }

    @Test fun changedTermsAndMissingRequiredConsentDoNotSendSignup() = runBlocking {
        newLogin()
        assertEquals(AuthResult.Failure(AuthFailure.CONSENT_CHANGED), repository.signup(documents, setOf("7:1")))
        current = documents.map { it.copy(version = 2) }
        assertEquals(AuthResult.Failure(AuthFailure.CONSENT_CHANGED), repository.signup(documents, setOf("7:1", "8:1")))
        assertEquals(1, server.requestCount)
    }

    @Test fun processDeathDropsSignupTokenAndRequiresLogin() = runBlocking {
        assertEquals(AuthResult.Failure(AuthFailure.UNAUTHORIZED), repository.signup(documents, setOf("7:1", "8:1")))
        assertEquals(0, server.requestCount)
    }

    @Test fun simultaneousRejectedTokensRefreshOnceAndRotatePersistedTokens() = runBlocking {
        existingLogin()
        server.enqueue(tokenResponse("next", "next-refresh"))
        val results = (1..8).map { async { repository.refresh("access") } }.awaitAll()
        assertTrue(results.all { it == AuthResult.Success("next") })
        assertEquals(2, server.requestCount)
        assertEquals("next-refresh", storage.value?.refreshToken)
    }

    @Test fun invalidRefreshClearsSessionButServerFailurePreservesIt() = runBlocking {
        existingLogin()
        server.enqueue(MockResponse().setResponseCode(503))
        assertEquals(AuthResult.Failure(AuthFailure.SERVER), repository.refresh("access"))
        assertNotNull(storage.value)
        server.enqueue(MockResponse().setResponseCode(401))
        assertEquals(AuthResult.Failure(AuthFailure.UNAUTHORIZED), repository.refresh("access"))
        assertNull(storage.value)
        assertEquals(OwnerSession.SIGNED_OUT, repository.session.value)
    }

    @Test fun restartRefreshesStoredTokensWithoutKakaoLogin() = runBlocking {
        storage.value = OwnerTokens("old", "saved-refresh")
        server.enqueue(tokenResponse())
        assertEquals(AuthResult.Success(OwnerSession.AUTHENTICATED), repository.restore())
        val request = server.takeRequest()
        assertEquals("/auth/refresh", request.path)
        assertNull(request.getHeader("Authorization"))
        assertTrue(request.body.readUtf8().contains("saved-refresh"))
    }

    @Test fun emptyTokenResponseCannotCreateSession() = runBlocking {
        server.enqueue(response("""{"registered":true}"""))
        assertEquals(AuthResult.Failure(AuthFailure.INVALID_RESPONSE), repository.login("kakao"))
        assertNull(storage.value)
    }

    @Test fun unauthorizedRequestRetriesOnlyOnceWithRotatedToken() = runBlocking {
        existingLogin()
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(tokenResponse("next", "next-refresh"))
        server.enqueue(MockResponse().setResponseCode(401))
        val client = OkHttpClient.Builder().addInterceptor(OwnerSessionInterceptor(repository, server.url("/"))).build()
        client.newCall(Request.Builder().url(server.url("/owner/products")).build()).execute().use { assertEquals(401, it.code) }
        assertEquals("Bearer access", server.takeRequest().getHeader("Authorization"))
        assertNull(server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer next", server.takeRequest().getHeader("Authorization"))
        assertEquals(4, server.requestCount)
    }

    @Test fun logoutClearsLocalSessionEvenIfServerFails() = runBlocking {
        existingLogin()
        server.enqueue(MockResponse().setResponseCode(503))
        assertEquals(AuthResult.Failure(AuthFailure.SERVER), repository.logout())
        assertNull(storage.value)
        assertEquals(OwnerSession.SIGNED_OUT, repository.session.value)
    }

    @Test fun offlineRestorationKeepsStoredRefreshToken() = runBlocking {
        storage.value = OwnerTokens("old", "saved-refresh")
        server.shutdown()
        assertEquals(AuthResult.Failure(AuthFailure.NETWORK), repository.restore())
        assertEquals("saved-refresh", storage.value?.refreshToken)
    }

    @Test fun foreignOriginDoesNotReceiveMangroToken() = runBlocking {
        existingLogin()
        server.enqueue(MockResponse().setResponseCode(200))
        val client = OkHttpClient.Builder().addInterceptor(OwnerSessionInterceptor(repository)).build()
        client.newCall(Request.Builder().url(server.url("/unrelated")).build()).execute().close()
        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test fun unreadableEncryptedSessionReturnsToLoginInsteadOfRetryingForever() = runBlocking {
        storage.value = OwnerTokens("old", "saved-refresh")
        storage.readFailure = java.security.GeneralSecurityException()
        assertEquals(AuthResult.Success(OwnerSession.SIGNED_OUT), repository.restore())
        assertNull(storage.value)
        assertEquals(0, server.requestCount)
    }
}
