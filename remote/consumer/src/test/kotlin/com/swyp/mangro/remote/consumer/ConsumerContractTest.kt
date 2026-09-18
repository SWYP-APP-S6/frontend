package com.swyp.mangro.remote.consumer

import com.swyp.mangro.core.network.Constants
import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.core.network.error.readHttpError
import com.swyp.mangro.remote.consumer.model.RecipeSummaryResponse
import com.swyp.mangro.remote.consumer.serivce.ConsumerServices
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Retrofit

class ConsumerContractTest {
    private val json = NetworkModule.provideNetworkJson()

    private fun createRetrofit(
        baseUrl: String = Constants.BASE_URL,
        client: OkHttpClient = OkHttpClient(),
    ): Retrofit = NetworkModule.provideRetrofit(client, json).newBuilder().baseUrl(baseUrl).build()

    @Test
    fun pathAndCoordinatesArePreserved() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":{}}"""))
            ConsumerServices(createRetrofit(server.url("/").toString())).product
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
            val error = ConsumerServices(createRetrofit(server.url("/").toString())).hold
                .cancelHold(7L).readHttpError(json)
            assertEquals("CANCEL_LIMIT_EXCEEDED", error?.code)
            assertEquals("\"2026-09-14T05:47:46Z\"", error?.body?.get("retryAt").toString())
            assertEquals("POST", server.takeRequest(5, TimeUnit.SECONDS)?.method)
        }
    }

    @Test
    fun paginationUsesPageAndSizeWithoutSort() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":{}}"""))
            ConsumerServices(createRetrofit(server.url("/").toString())).hold
                .fetchHolds(page = 0, size = 20)
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("0", request.requestUrl?.queryParameter("page"))
            assertEquals("20", request.requestUrl?.queryParameter("size"))
            assertNull(request.requestUrl?.queryParameter("sort"))
            assertNull(request.requestUrl?.queryParameter("pageable"))
        }
    }

    @Test
    fun recipesKeepCategoryAndUseV2PaginationDefaults() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":{}}"""))
            ConsumerServices(createRetrofit(server.url("/").toString())).recipe.fetchRecipes(category = "SOUP")
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("/recipes", request.requestUrl?.encodedPath)
            assertEquals("SOUP", request.requestUrl?.queryParameter("category"))
            assertEquals("0", request.requestUrl?.queryParameter("page"))
            assertEquals("20", request.requestUrl?.queryParameter("size"))
            assertNull(request.requestUrl?.queryParameter("sort"))
            assertNull(request.requestUrl?.queryParameter("pageable"))
        }
    }

    @Test
    fun defaultsPreserveNullableAndRejectExplicitNullForRequiredField() {
        val recipe = json.decodeFromString<RecipeSummaryResponse>("{}")
        assertNull(recipe.cookTimeMinutes)
        assertEquals(0L, recipe.id)
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<RecipeSummaryResponse>("""{"id":null}""")
        }
    }
}
