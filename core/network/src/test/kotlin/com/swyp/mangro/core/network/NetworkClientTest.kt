package com.swyp.mangro.core.network

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkClientTest {
    @Test
    fun productionBaseUrlIsConfigured() {
        assertEquals("https://api.mangro.cloud/", NetworkClient.create().baseUrl().toString())
    }

    @Test
    fun authenticatedClientReadsLatestTokenAndPreservesExplicitHeader() {
        MockWebServer().use { server ->
            var token: String? = "first"
            val client = OkHttpClient.Builder().addInterceptor(BearerTokenInterceptor { token }).build()
            fun send(header: String? = null): String? {
                server.enqueue(MockResponse())
                val builder = Request.Builder().url(server.url("/"))
                header?.let { builder.header("Authorization", it) }
                client.newCall(builder.build()).execute().close()
                return server.takeRequest(5, TimeUnit.SECONDS)?.getHeader("Authorization")
            }
            assertEquals("Bearer first", send())
            token = "second"
            assertEquals("Bearer second", send())
            assertEquals("Bearer explicit", send("Bearer explicit"))
            token = null
            assertNull(send())
        }
    }
}
