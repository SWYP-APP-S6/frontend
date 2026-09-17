package com.swyp.mangro.data.owner.product

import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.data.owner.product.impl.ProductPhotoSource
import com.swyp.mangro.data.owner.product.impl.ProductRepositoryImpl
import com.swyp.mangro.data.owner.product.model.ProductRegistration
import com.swyp.mangro.remote.owner.service.ProductService
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductRepositoryTest {
    private val json = NetworkModule.provideNetworkJson()
    private val draft = ProductRegistration("상품", listOf("first", "second"), 3, 3000, 2000, "2026-09-17T20:00:00+09:00", "BAKERY", listOf(12, 34))
    private val reads = mutableListOf<String>()
    private val source = object : ProductPhotoSource {
        override fun part(uri: String): MultipartBody.Part {
            reads += uri
            return MultipartBody.Part.createFormData("file", "photo.jpg", "image-content".toRequestBody("image/jpeg".toMediaType()))
        }
    }

    private fun repository(server: MockWebServer): ProductRepositoryImpl {
        val retrofit = NetworkModule.provideRetrofit(OkHttpClient(), json).newBuilder().baseUrl(server.url("/")).build()
        return ProductRepositoryImpl(retrofit.create(ProductService::class.java), source)
    }

    @Test fun uploadsOnlyFirstPhotoAndRegistersReturnedUrlAndTagIds() = runTest {
        MockWebServer().use { server ->
            server.enqueue(ok("""{"photoUrl":"https://example.test/photo.jpg"}"""))
            server.enqueue(ok("""{"id":91,"name":"서버 상품","photoUrl":"https://example.test/photo.jpg","initialQty":3,"stockQty":2,"heldQty":1,"completedQty":1,"originalPrice":3000,"salePrice":2000,"pickupEndAt":"2026-09-17T20:00:00+09:00"}"""))
            val result = repository(server).register(draft).first().getOrThrow()
            assertEquals(91L, result.id)
            assertEquals("서버 상품", result.name)
            assertEquals(2, result.stockQuantity)
            assertEquals(1, result.completedQuantity)
            assertEquals(listOf("first"), reads)
            val upload = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("/owner/products/photos", upload.path)
            assertTrue(upload.body.readUtf8().contains("name=\"file\""))
            val registration = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("POST", registration.method)
            assertEquals("/owner/products", registration.path)
            val body = json.parseToJsonElement(registration.body.readUtf8()).jsonObject
            assertEquals("https://example.test/photo.jpg", body.getValue("photoUrl").jsonPrimitive.content)
            assertEquals(listOf("12", "34"), body.getValue("ingredientTags").jsonArray.map { it.jsonPrimitive.content })
            assertEquals("BAKERY", body.getValue("category").jsonPrimitive.content)
            assertEquals(draft.pickupEndAt, body.getValue("pickupEndAt").jsonPrimitive.content)
        }
    }

    @Test fun emptyPhotoListSkipsUpload() = runTest {
        MockWebServer().use { server ->
            server.enqueue(ok("""{"id":92}"""))
            assertTrue(repository(server).register(draft.copy(photos = emptyList())).first().isSuccess)
            assertTrue(reads.isEmpty())
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("/owner/products", request.path)
            assertEquals("", json.parseToJsonElement(request.body.readUtf8()).jsonObject.getValue("photoUrl").jsonPrimitive.content)
        }
    }

    @Test fun uploadFailureStopsBeforeRegistration() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(500))
            assertTrue(repository(server).register(draft).first().isFailure)
            assertEquals(1, server.requestCount)
        }
    }

    @Test fun registrationFailureIsNotReportedAsSuccess() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(403))
            assertTrue(repository(server).register(draft.copy(photos = emptyList())).first().isFailure)
        }
    }

    @Test fun preparedFoodUsesSideDishWireCategory() = runTest {
        MockWebServer().use { server ->
            server.enqueue(ok("""{"id":93}"""))
            assertTrue(repository(server).register(draft.copy(photos = emptyList(), category = "PREPARED_FOOD")).first().isSuccess)
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("SIDE_DISH", json.parseToJsonElement(request.body.readUtf8()).jsonObject.getValue("category").jsonPrimitive.content)
        }
    }

    @Test fun unknownCategoryFailsBeforeUpload() = runTest {
        MockWebServer().use { server ->
            assertTrue(repository(server).register(draft.copy(category = "UNKNOWN")).first().isFailure)
            assertTrue(reads.isEmpty())
            assertEquals(0, server.requestCount)
        }
    }

    private fun ok(data: String) = MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":$data}""")
}
