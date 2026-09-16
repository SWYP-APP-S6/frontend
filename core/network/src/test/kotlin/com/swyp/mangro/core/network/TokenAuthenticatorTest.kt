package com.swyp.mangro.core.network

import com.swyp.mangro.core.network.interceptor.AuthorizationInterceptor
import com.swyp.mangro.core.network.provider.TokenProvider
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TokenAuthenticatorTest {
    private class Tokens : TokenProvider {
        var refreshes = 0
        var token = TokenFixtures.ACCESS_TOKEN
        override fun accessToken(): String = token
        override fun refreshAccessToken(failedAccessToken: String): String? {
            if (failedAccessToken != token) return null
            refreshes++
            token = TokenFixtures.NEW_ACCESS_TOKEN
            return token
        }
    }

    private fun client(server: MockWebServer, tokens: Tokens): OkHttpClient {
        val baseUrl = server.url("/")
        return OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor(baseUrl) { tokens.accessToken() })
            .authenticator(TokenAuthenticator(tokens, baseUrl))
            .build()
    }

    @Test
    fun unauthorizedRetriesOnceWithNewTokenAndStopsOnSecondUnauthorized() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(401))
            server.enqueue(MockResponse().setResponseCode(401))
            val tokens = Tokens()
            client(server, tokens).newCall(Request.Builder().url(server.url("/users/me")).build()).execute().use {
                assertEquals(401, it.code)
            }
            assertEquals("Bearer ${TokenFixtures.ACCESS_TOKEN}", server.takeRequest().getHeader("Authorization"))
            val retry = server.takeRequest()
            assertEquals("Bearer ${TokenFixtures.NEW_ACCESS_TOKEN}", retry.getHeader("Authorization"))
            assertNull(retry.getHeader("Accept"))
            assertEquals(1, tokens.refreshes)
        }
    }

    @Test
    fun explicitStoredTokenCanRefreshWithoutRequestTag() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(401))
            server.enqueue(MockResponse())
            val tokens = Tokens()
            client(server, tokens).newCall(
                Request.Builder().url(server.url("/users/me"))
                    .header("Authorization", "bearer ${TokenFixtures.ACCESS_TOKEN}").build(),
            ).execute().use { assertEquals(200, it.code) }
            assertEquals("bearer ${TokenFixtures.ACCESS_TOKEN}", server.takeRequest().getHeader("Authorization"))
            assertEquals("Bearer ${TokenFixtures.NEW_ACCESS_TOKEN}", server.takeRequest().getHeader("Authorization"))
            assertEquals(1, tokens.refreshes)
        }
    }

    @Test
    fun redirectBeforeFirstUnauthorizedDoesNotConsumeAuthRetry() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(302).addHeader("Location", "/users/me"))
            server.enqueue(MockResponse().setResponseCode(401))
            server.enqueue(MockResponse())
            val tokens = Tokens()
            client(server, tokens).newCall(Request.Builder().url(server.url("/start")).build()).execute().use {
                assertEquals(200, it.code)
            }
            assertEquals(3, server.requestCount)
            assertEquals(1, tokens.refreshes)
        }
    }

    @Test
    fun absentOrNonBearerHeadersDoNotRefresh() {
        MockWebServer().use { server ->
            val tokens = Tokens()
            val client = OkHttpClient.Builder().authenticator(TokenAuthenticator(tokens, server.url("/"))).build()
            listOf(null, "Basic dGVzdDp0ZXN0", "Bearer ").forEach { header ->
                server.enqueue(MockResponse().setResponseCode(401))
                val builder = Request.Builder().url(server.url("/users/me"))
                header?.let { builder.header("Authorization", it) }
                client.newCall(builder.build()).execute().use { assertEquals(401, it.code) }
            }
            assertEquals(0, tokens.refreshes)
        }
    }

    @Test
    fun everyApiPathReceivesBearerWithoutInventedAuthExceptions() {
        MockWebServer().use { server ->
            val tokens = Tokens()
            val client = client(server, tokens)
            listOf("/users/me", "/auth/guest", "/auth/signup", "/auth/refresh", "/auth/logout", "/auth/consumer/kakao", "/terms").forEach { path ->
                server.enqueue(MockResponse())
                client.newCall(Request.Builder().url(server.url(path)).build()).execute().close()
                assertEquals("Bearer ${TokenFixtures.ACCESS_TOKEN}", server.takeRequest().getHeader("Authorization"))
            }
        }
    }

    @Test
    fun logoutFollowsTheSameRefreshAndRetryFlow() {
        MockWebServer().use { server ->
            val tokens = Tokens()
            server.enqueue(MockResponse().setResponseCode(401))
            server.enqueue(MockResponse())
            client(server, tokens).newCall(Request.Builder().url(server.url("/auth/logout")).build()).execute().use {
                assertEquals(200, it.code)
            }
            assertEquals("Bearer ${TokenFixtures.ACCESS_TOKEN}", server.takeRequest().getHeader("Authorization"))
            assertEquals("Bearer ${TokenFixtures.NEW_ACCESS_TOKEN}", server.takeRequest().getHeader("Authorization"))
            assertEquals(1, tokens.refreshes)
        }
    }

    @Test
    fun failedOrUnusableRefreshReturnsOriginalUnauthorizedWithoutResending() {
        val results = listOf<String?>(null, "", "  ", TokenFixtures.ACCESS_TOKEN)
        results.forEach { result -> assertRefreshStops { result } }
        assertRefreshStops { throw java.io.IOException("Test refresh failure") }
    }

    private fun assertRefreshStops(refresh: () -> String?) {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(401))
            val provider = object : TokenProvider {
                override fun accessToken(): String = TokenFixtures.ACCESS_TOKEN
                override fun refreshAccessToken(failedAccessToken: String): String? = refresh()
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthorizationInterceptor(server.url("/")) { provider.accessToken() })
                .authenticator(TokenAuthenticator(provider, server.url("/")))
                .build()
            client.newCall(Request.Builder().url(server.url("/users/me")).build()).execute().use {
                assertEquals(401, it.code)
            }
            assertEquals(1, server.requestCount)
        }
    }

    @Test
    fun foreignOriginAndCrossOriginRedirectNeverReceiveStoredToken() {
        MockWebServer().use { api ->
            MockWebServer().use { external ->
                val tokens = Tokens()
                val client = client(api, tokens)
                external.enqueue(MockResponse().setResponseCode(401))
                client.newCall(Request.Builder().url(external.url("/users/me")).build()).execute().close()
                assertNull(external.takeRequest().getHeader("Authorization"))
                api.enqueue(MockResponse().setResponseCode(302).addHeader("Location", external.url("/redirect")))
                external.enqueue(MockResponse().setResponseCode(401))
                client.newCall(Request.Builder().url(api.url("/users/me")).build()).execute().close()
                assertEquals("Bearer ${TokenFixtures.ACCESS_TOKEN}", api.takeRequest().getHeader("Authorization"))
                assertNull(external.takeRequest(5, TimeUnit.SECONDS)?.getHeader("Authorization"))
                assertEquals(0, tokens.refreshes)
            }
        }
    }

    @Test
    fun okHttpDoesNotReplayOneShotBodyAfterRefresh() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(401))
            val tokens = Tokens()
            val body = object : okhttp3.RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaType()
                override fun isOneShot(): Boolean = true
                override fun writeTo(sink: okio.BufferedSink) {
                    sink.writeUtf8("test upload")
                }
            }
            client(server, tokens).newCall(Request.Builder().url(server.url("/uploads")).post(body).build()).execute().close()
            assertEquals("application/octet-stream", server.takeRequest().getHeader("Content-Type"))
            assertEquals(1, tokens.refreshes)
            assertEquals(1, server.requestCount)
        }
    }

    @Test
    fun forbiddenDoesNotRefreshAndExplicitAcceptIsPreserved() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(403))
            val tokens = Tokens()
            client(server, tokens).newCall(Request.Builder().url(server.url("/users/me")).header("Accept", "text/plain").build()).execute().close()
            assertEquals("text/plain", server.takeRequest().getHeader("Accept"))
            assertEquals(0, tokens.refreshes)
        }
    }
}
