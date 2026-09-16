package com.swyp.mangro.data.owner.store

import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.core.network.interceptor.AuthorizationInterceptor
import com.swyp.mangro.data.owner.store.impl.StoreRepositoryImpl
import com.swyp.mangro.data.owner.store.model.StoreRegistration
import com.swyp.mangro.remote.owner.service.StoreService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class StoreRepositoryTest {
    private val server = MockWebServer()
    private val repository by lazy {
        val client = OkHttpClient.Builder().addInterceptor(AuthorizationInterceptor(server.url("/")) { "app-access" }).build()
        val retrofit = NetworkModule.provideRetrofit(client, NetworkModule.provideNetworkJson()).newBuilder().baseUrl(server.url("/")).build()
        StoreRepositoryImpl(retrofit.create(StoreService::class.java))
    }
    private val registration = StoreRegistration("상점", "FRUIT", "03965", "서울 마포구 망원로 12", "1층", "0212345678", 540, 1200, setOf(0, 1, 6))

    @After fun close() = server.shutdown()

    private fun success(data: String = """{"id":1,"status":"PENDING"}""") {
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"status":201,"code":"CREATED","data":$data}"""))
    }

    @Test fun fetchesStoreAndOnlyExactApprovedStatusAllowsRegistration() = runTest {
        for (status in listOf("PENDING", "APPROVED", "REJECTED", "NEW_STATUS", "")) {
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":{"id":9,"name":"상점","status":"$status","businessOpenTime":"09:00:00","businessCloseTime":"20:00:00"}}"""))
            val store = repository.fetchMyStore().single().getOrThrow()
            assertEquals("/owner/stores/me", server.takeRequest().path)
            assertEquals(status == "APPROVED", store.canRegisterProduct)
            assertEquals("09:00:00", store.businessOpenTime)
        }
        success("null")
        assertTrue(repository.fetchMyStore().single().isFailure)
        server.enqueue(MockResponse().setResponseCode(403))
        assertTrue(repository.fetchMyStore().single().isFailure)
    }

    @Test fun sendsServerCategoryAddressHoursDaysAndEmptyOptionalInputsWithAppToken() = runTest {
        for (category in listOf("VEGETABLE", "FRUIT", "MEAT", "SEAFOOD", "DAIRY_EGG", "BAKERY", "PREPARED_FOOD", "ETC")) {
            success()
            assertTrue(repository.register(registration.copy(categoryId = category)).single().isSuccess)
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/owner/stores", request.path)
            assertEquals("Bearer app-access", request.getHeader("Authorization"))
            val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
            assertEquals(listOf(category), body.getValue("categories").jsonArray.map { it.jsonPrimitive.content })
            assertEquals(listOf("SUNDAY", "MONDAY", "SATURDAY"), body.getValue("businessDays").jsonArray.map { it.jsonPrimitive.content })
            assertEquals("09:00:00", body.getValue("businessOpenTime").jsonPrimitive.content)
            assertEquals("20:00:00", body.getValue("businessCloseTime").jsonPrimitive.content)
            assertEquals("03965", body.getValue("postalCode").jsonPrimitive.content)
            assertEquals("1층", body.getValue("addressDetail").jsonPrimitive.content)
            assertEquals("", body.getValue("businessRegistrationNumber").jsonPrimitive.content)
            assertEquals("", body.getValue("applicationNote").jsonPrimitive.content)
        }
    }

    @Test fun sampleCategoryAndInvalidTimesNeverCallServer() = runTest {
        assertTrue(repository.register(registration.copy(categoryId = "debug-1")).single().isFailure)
        assertTrue(repository.register(registration.copy(closingMinutes = 0)).single().isFailure)
        assertTrue(repository.register(registration.copy(businessDays = setOf(7))).single().isFailure)
        assertEquals(0, server.requestCount)
    }

    @Test fun serverRejectionAndIncompleteSuccessNeverReportRegistered() = runTest {
        server.enqueue(MockResponse().setResponseCode(400))
        assertTrue(repository.register(registration).single().isFailure)
        success("null")
        assertTrue(repository.register(registration).single().isFailure)
        success("{}")
        assertTrue(repository.register(registration).single().isFailure)
    }

    @Test fun registrationFlowIsColdAndCollectorErrorsPropagate() = runTest {
        val result = repository.register(registration)
        assertEquals(0, server.requestCount)
        success()
        val expected = IllegalArgumentException("collector")
        try {
            result.collect { throw expected }
            fail("Collector error must propagate")
        } catch (error: IllegalArgumentException) {
            assertEquals(expected.message, error.message)
        }
        assertEquals(1, server.requestCount)
    }
}
