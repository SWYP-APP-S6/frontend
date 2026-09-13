package com.swyp.mangro.remote.consumer

import com.swyp.mangro.core.network.NetworkClient
import com.swyp.mangro.core.network.readHttpError
import com.swyp.mangro.remote.consumer.di.ConsumerServices
import com.swyp.mangro.remote.consumer.model.RecipeSummaryResponse
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ConsumerContractTest {
    @Test
    fun pathAndCoordinatesArePreserved() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":{}}"""))
            ConsumerServices(NetworkClient.create(server.url("/").toString())).product
                .fetchProduct(42L, 37.123456, 127.123456)
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("GET", request.method)
            assertEquals("/products/42", request.requestUrl?.encodedPath)
            assertEquals("37.123456", request.requestUrl?.queryParameter("lat"))
            assertEquals("127.123456", request.requestUrl?.queryParameter("lng"))
        }
    }

    @Test
    fun conflictPreservesRetryAtAndFieldErrors() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(409).setBody("""{"status":409,"code":"CANCEL_LIMIT_EXCEEDED","message":"wait","retryAt":"2026-09-14T05:47:46Z","fieldErrors":{"qty":"invalid"}}"""))
            val error = ConsumerServices(NetworkClient.create(server.url("/").toString())).hold
                .cancelHold(7L).readHttpError()
            assertEquals("CANCEL_LIMIT_EXCEEDED", error?.code)
            assertEquals("\"2026-09-14T05:47:46Z\"", error?.body?.get("retryAt").toString())
            assertEquals("POST", server.takeRequest(5, TimeUnit.SECONDS)?.method)
        }
    }

    @Test
    fun pageableUsesSeparateQueriesAndRepeatedSort() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":{}}"""))
            ConsumerServices(NetworkClient.create(server.url("/").toString())).hold
                .fetchHolds(page = 0, size = 20, sort = listOf("id,desc", "heldAt,asc"))
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("0", request.requestUrl?.queryParameter("page"))
            assertEquals("20", request.requestUrl?.queryParameter("size"))
            assertEquals(listOf("id,desc", "heldAt,asc"), request.requestUrl?.queryParameterValues("sort"))
            assertNull(request.requestUrl?.queryParameter("pageable"))
        }
    }

    @Test
    fun defaultsPreserveNullableAndRejectExplicitNullForRequiredField() {
        val recipe = NetworkClient.json.decodeFromString<RecipeSummaryResponse>("{}")
        assertNull(recipe.cookTimeMinutes)
        assertEquals(0L, recipe.id)
        assertThrows(SerializationException::class.java) {
            NetworkClient.json.decodeFromString<RecipeSummaryResponse>("""{"id":null}""")
        }
    }
}
