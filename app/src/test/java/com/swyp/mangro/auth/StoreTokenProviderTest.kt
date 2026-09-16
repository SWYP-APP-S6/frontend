package com.swyp.mangro.auth

import com.swyp.core.local.model.AuthKey
import com.swyp.core.local.store.AuthStore
import com.swyp.mangro.core.network.Constants
import com.swyp.mangro.core.network.TokenAuthenticator
import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.core.network.exception.TokenRefreshException
import com.swyp.mangro.core.network.interceptor.AuthorizationInterceptor
import com.swyp.mangro.remote.auth.service.TokenRefreshService
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class StoreTokenProviderTest {
    private val json = NetworkModule.provideNetworkJson()

    private fun createRetrofit(
        baseUrl: String = Constants.BASE_URL,
        client: OkHttpClient = OkHttpClient(),
    ): Retrofit = NetworkModule.provideRetrofit(client, json).newBuilder().baseUrl(baseUrl).build()

    private val original = AuthKey(TokenFixtures.ACCESS_TOKEN, TokenFixtures.REFRESH_TOKEN)
    private val updated = AuthKey(TokenFixtures.NEW_ACCESS_TOKEN, TokenFixtures.NEW_REFRESH_TOKEN)

    private class MemoryStore(initial: AuthKey) : AuthStore {
        override val authKey = MutableStateFlow<AuthKey?>(initial)
        override suspend fun save(authKey: AuthKey) {
            this.authKey.value = authKey
        }
        override suspend fun clear() {
            authKey.value = null
        }
        override suspend fun replaceIfMatches(expected: AuthKey, updated: AuthKey): Boolean = authKey.compareAndSet(expected, updated)
    }

    private fun provider(server: MockWebServer, store: AuthStore): StoreTokenProvider = StoreTokenProvider(
        refreshService = createRetrofit(baseUrl = server.url("/").toString()).create(TokenRefreshService::class.java),
        authStore = store,
        json = json,
    )

    private fun success(): MockResponse = MockResponse().setBody(
        """{"status":200,"code":"OK","message":"ok","data":{"accessToken":"${updated.accessToken}","refreshToken":"${updated.refreshToken}"}}""",
    )

    @Test
    fun concurrentUnauthorizedCallsRefreshOnceAndPersistBothTokens() {
        MockWebServer().use { server ->
            server.enqueue(success().setBodyDelay(100, TimeUnit.MILLISECONDS))
            val store = MemoryStore(original)
            val provider = provider(server, store)
            val executor = Executors.newFixedThreadPool(8)
            try {
                val tasks = (1..8).map { executor.submit<String?> { provider.refreshAccessToken(original.accessToken) } }
                tasks.forEach { assertEquals(updated.accessToken, it.get(5, TimeUnit.SECONDS)) }
                assertEquals(updated, store.authKey.value)
                assertEquals(1, server.requestCount)
                val request = server.takeRequest()
                assertEquals("/auth/refresh", request.path)
                assertEquals("""{"refreshToken":"${original.refreshToken}"}""", request.body.readUtf8())
                assertEquals("Bearer ${original.accessToken}", request.getHeader("Authorization"))
            } finally {
                executor.shutdownNow()
            }
        }
    }

    @Test
    fun concurrentFailedRefreshSharesErrorAndLaterCallCanRetry() {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setResponseCode(429).addHeader("Retry-After", "10")
                    .setBody("""{"status":429,"code":"RATE_LIMITED","message":"test"}""")
                    .setBodyDelay(500, TimeUnit.MILLISECONDS),
            )
            val store = MemoryStore(original)
            val provider = provider(server, store)
            val executor = Executors.newFixedThreadPool(8)
            val start = java.util.concurrent.CyclicBarrier(8)
            try {
                val tasks = (1..8).map {
                    executor.submit<Throwable?> {
                        start.await(5, TimeUnit.SECONDS)
                        runCatching { provider.refreshAccessToken(original.accessToken) }.exceptionOrNull()
                    }
                }
                tasks.forEach {
                    val failure = it.get(5, TimeUnit.SECONDS) as TokenRefreshException
                    assertEquals(429, failure.error.httpStatus)
                    assertEquals("10", failure.error.retryAfter)
                }
                assertEquals(1, server.requestCount)
                assertEquals(original, store.authKey.value)
                server.enqueue(success())
                assertEquals(updated.accessToken, provider.refreshAccessToken(original.accessToken))
                assertEquals(2, server.requestCount)
            } finally {
                executor.shutdownNow()
            }
        }
    }

    @Test
    fun refreshDoesNotRestoreSessionClearedDuringRequest() {
        MockWebServer().use { server ->
            val store = MemoryStore(original)
            server.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    runBlocking { store.clear() }
                    return success()
                }
            }
            assertNull(provider(server, store).refreshAccessToken(original.accessToken))
            assertNull(store.authKey.value)
        }
    }

    @Test
    fun oldRequestDoesNotUseNewlySignedInAccount() {
        MockWebServer().use { server ->
            val store = MemoryStore(updated)
            assertNull(provider(server, store).refreshAccessToken(original.accessToken))
            assertEquals(0, server.requestCount)
        }
    }

    @Test
    fun failedRefreshRetainsSessionAndDoesNotRetryRecursively() {
        MockWebServer().use { server ->
            listOf(401, 403, 429, 500).forEach { code ->
                server.enqueue(MockResponse().setResponseCode(code).addHeader("Retry-After", "30").setBody("""{"status":$code,"code":"REFRESH_FAILED","message":"test"}"""))
                val store = MemoryStore(original)
                val failure = runCatching { provider(server, store).refreshAccessToken(original.accessToken) }.exceptionOrNull() as TokenRefreshException
                assertEquals(code, failure.error.httpStatus)
                assertEquals("REFRESH_FAILED", failure.error.code)
                assertEquals("30", failure.error.retryAfter)
                assertEquals(original, store.authKey.value)
            }
            assertEquals(4, server.requestCount)
        }
    }

    @Test
    fun persistenceFailureIsAnIoFailureAndDoesNotExposeUnsavedToken() {
        MockWebServer().use { server ->
            server.enqueue(success())
            val originalStore = MemoryStore(original)
            val failingStore = object : AuthStore by originalStore {
                override suspend fun replaceIfMatches(expected: AuthKey, updated: AuthKey): Boolean = error("Test storage failure")
            }
            val failure = runCatching { provider(server, failingStore).refreshAccessToken(original.accessToken) }.exceptionOrNull()
            assertTrue(failure is IOException)
            assertEquals(original, originalStore.authKey.value)
        }
    }

    @Test
    fun emptySessionAndStorageErrorsRemainDistinct() {
        MockWebServer().use { server ->
            val store = MemoryStore(original)
            val provider = provider(server, store)
            assertEquals(original.accessToken, provider.accessToken())
            runBlocking { store.clear() }
            assertNull(provider.accessToken())
            assertNull(provider.refreshAccessToken(original.accessToken))
            val failing = object : AuthStore by store {
                override val authKey = kotlinx.coroutines.flow.flow<AuthKey?> { error("Test read failure") }
            }
            assertTrue(runCatching { provider(server, failing).accessToken() }.exceptionOrNull() is IOException)
            assertEquals(0, server.requestCount)
        }
    }

    @Test
    fun incompleteRefreshResponseDoesNotReplaceTokens() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":200,"data":{"accessToken":"","refreshToken":""}}"""))
            val store = MemoryStore(original)
            assertTrue(runCatching { provider(server, store).refreshAccessToken(original.accessToken) }.exceptionOrNull() is IOException)
            assertEquals(original, store.authKey.value)
        }
    }

    @Test
    fun originalRequestSucceedsOnlyAfterRefreshedTokensAreSaved() {
        MockWebServer().use { api ->
            MockWebServer().use { refresh ->
                api.enqueue(MockResponse().setResponseCode(401))
                api.enqueue(MockResponse().setResponseCode(200))
                refresh.enqueue(success())
                val store = MemoryStore(original)
                val provider = provider(refresh, store)
                val baseUrl = api.url("/")
                val client = OkHttpClient.Builder()
                    .addInterceptor(AuthorizationInterceptor(baseUrl) { provider.accessToken() })
                    .authenticator(TokenAuthenticator(provider, baseUrl))
                    .build()
                client.newCall(Request.Builder().url(api.url("/users/me")).build()).execute().use {
                    assertEquals(200, it.code)
                }
                assertEquals("Bearer ${original.accessToken}", api.takeRequest().getHeader("Authorization"))
                assertEquals("Bearer ${updated.accessToken}", api.takeRequest().getHeader("Authorization"))
                assertEquals(updated, store.authKey.value)
            }
        }
    }
}
