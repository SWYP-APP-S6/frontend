package com.swyp.mangro.remote.owner

import com.swyp.mangro.core.network.Constants
import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.remote.owner.model.AnswerStockReconfirmRequest
import com.swyp.mangro.remote.owner.model.PreviewProductRequest
import com.swyp.mangro.remote.owner.model.ProductDetailResponse
import com.swyp.mangro.remote.owner.model.ProductPreviewResponse
import com.swyp.mangro.remote.owner.model.RegisterProductRequest
import com.swyp.mangro.remote.owner.model.UpdateStockRequest
import com.swyp.mangro.remote.owner.service.OwnerServices
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit

class OwnerContractTest {
    private val json = NetworkModule.provideNetworkJson()

    @Test
    fun ingredientSearchAndRecommendationsUnwrapObjectLists() = runTest {
        MockWebServer().use { server ->
            val ingredient = OwnerServices(createRetrofit(server.url("/").toString())).ingredient
            repeat(2) { server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":[{"id":12,"name":"당근","category":"VEGETABLE"}]}""")) }
            val tags = ingredient.searchIngredients(query = "carrot", size = 7).body()!!
            assertEquals(12, tags.single().id)
            assertEquals("당근", tags.single().name)
            assertEquals("VEGETABLE", tags.single().category)
            var request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("GET", request.method)
            assertEquals("/owner/ingredients?query=carrot&size=7", request.path)
            assertEquals(tags, ingredient.recommendIngredientTags(name = "soup").body())
            request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("GET", request.method)
            assertEquals("/owner/ingredients/recommendations?name=soup", request.path)
        }
    }

    @Test
    fun detailAndPreviewDecodeIngredientObjects() {
        val body = """{"ingredientTags":[{"id":12,"name":"당근"}]}"""
        val detail = json.decodeFromString<ProductDetailResponse>(body)
        val preview = json.decodeFromString<ProductPreviewResponse>(body)
        assertEquals(12, detail.ingredientTags.single().id)
        assertEquals("당근", detail.ingredientTags.single().name)
        assertEquals(null, detail.ingredientTags.single().category)
        assertEquals(detail.ingredientTags, preview.ingredientTags)
    }

    private fun createRetrofit(
        baseUrl: String = Constants.BASE_URL,
        client: OkHttpClient = OkHttpClient(),
    ): Retrofit = NetworkModule.provideRetrofit(client, json).newBuilder().baseUrl(baseUrl).build()

    @Test
    fun stockUpdateUsesOnlyTotalStock() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":{}}"""))
            OwnerServices(createRetrofit(server.url("/").toString())).product
                .updateStock(9L, UpdateStockRequest(stockQty = 12))
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("PATCH", request.method)
            assertEquals("/owner/products/9/stock", request.path)
            assertEquals(json.parseToJsonElement("""{"stockQty":12}"""), json.parseToJsonElement(request.body.readUtf8()))
        }
    }

    @Test
    fun reconfirmPreservesFalseAndPreviewUsesSeparatePost() = runTest {
        MockWebServer().use { server ->
            val product = OwnerServices(createRetrofit(server.url("/").toString())).product
            repeat(2) { server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":{}}""")) }
            product.answerStockReconfirm(9L, AnswerStockReconfirmRequest(confirmed = false))
            var request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("/owner/products/9/stock-reconfirm", request.path)
            assertEquals("POST", request.method)
            assertEquals("""{"confirmed":false}""", request.body.readUtf8())
            product.previewProduct(PreviewProductRequest(photoUrl = "https://api.mangro.cloud/uploads/products/test.png"))
            request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("/owner/products/preview", request.path)
            assertEquals("POST", request.method)
        }
    }

    @Test
    fun photoUploadSendsMultipartFilePart() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":{"photoUrl":"https://api.mangro.cloud/uploads/products/test.png"}}"""))
            val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a)
            val part = MultipartBody.Part.createFormData("file", "test.png", bytes.toRequestBody("image/png".toMediaType()))
            val response = OwnerServices(createRetrofit(server.url("/").toString())).product.uploadProductPhoto(part)
            assertTrue(response.body()?.photoUrl?.endsWith("test.png") == true)
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("POST", request.method)
            assertEquals("/owner/products/photos", request.path)
            assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data; boundary="))
            val wire = request.body.readByteArray()
            val text = wire.toString(Charsets.ISO_8859_1)
            assertTrue(text.contains("name=\"file\"; filename=\"test.png\""))
            assertTrue(text.contains("Content-Type: image/png"))
            assertTrue(text.contains(bytes.toString(Charsets.ISO_8859_1)))
        }
    }

    @Test
    fun v2StockFieldsDecodeWithoutUsingDefaults() {
        val stock = json.decodeFromString<ProductDetailResponse>("""{"stockQty":2,"shortfallQty":3,"activeHoldQty":5,"reconfirmPending":true,"stockEditable":false,"minAdjustableQty":2}""")
        assertEquals(2, stock.stockQty)
        assertEquals(3, stock.shortfallQty)
        assertEquals(5L, stock.activeHoldQty)
        assertTrue(stock.reconfirmPending)
        assertFalse(stock.stockEditable)
        assertEquals(2, stock.minAdjustableQty)
    }

    @Test
    fun unknownEnumDoesNotSilentlyBecomeDefault() {
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<RegisterProductRequest>("""{"category":"FUTURE"}""")
        }
    }
}
