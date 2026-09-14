package com.swyp.mangro.remote.consumer

import com.swyp.mangro.core.network.NetworkClient
import com.swyp.mangro.remote.consumer.model.DeleteDeviceTokenRequest
import com.swyp.mangro.remote.consumer.model.HoldDetailResponse
import com.swyp.mangro.remote.consumer.model.HoldSummaryResponse
import com.swyp.mangro.remote.consumer.model.RecipeSummaryResponse
import com.swyp.mangro.remote.consumer.model.RegisterDeviceTokenRequest
import com.swyp.mangro.remote.consumer.model.RegisterHoldRequest
import com.swyp.mangro.remote.consumer.serivce.ConsumerServices
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsumerV2ContractTest {
    @Test
    fun deviceRegistrationAndDeletionBothSendJsonBody() = runTest {
        MockWebServer().use { server ->
            repeat(2) { server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":null}""")) }
            val service = ConsumerServices(NetworkClient.create(server.url("/").toString())).notification
            service.registerDeviceToken(RegisterDeviceTokenRequest(fcmToken = "test-only-token"))
            var request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("POST", request.method)
            assertEquals("/notifications/device-tokens", request.path)
            val registered = NetworkClient.json.parseToJsonElement(request.body.readUtf8()).jsonObject
            assertEquals("ANDROID", registered["platform"]?.jsonPrimitive?.content)
            assertEquals("test-only-token", registered["fcmToken"]?.jsonPrimitive?.content)
            service.deleteDeviceToken(DeleteDeviceTokenRequest(fcmToken = "test-only-token"))
            request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("DELETE", request.method)
            assertEquals("/notifications/device-tokens", request.path)
            assertEquals("""{"fcmToken":"test-only-token"}""", request.body.readUtf8())
        }
    }

    @Test
    fun holdRegistrationStillUsesProductIdAndQty() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":{}}"""))
            ConsumerServices(NetworkClient.create(server.url("/").toString())).hold
                .registerHold(RegisterHoldRequest(productId = 17L, qty = 2))
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("POST", request.method)
            assertEquals("/holds", request.path)
            assertEquals(NetworkClient.json.parseToJsonElement("""{"productId":17,"qty":2}"""), NetworkClient.json.parseToJsonElement(request.body.readUtf8()))
        }
    }

    @Test
    fun productHoldSummaryAndGroupDetailAreDifferentContracts() {
        val row = NetworkClient.json.decodeFromString<HoldSummaryResponse>("""{"productId":17,"productName":"test","photoUrl":"photo","qty":2,"cancelCreditUsed":true}""")
        assertEquals(17L, row.productId)
        assertEquals("test", row.productName)
        assertEquals(2, row.qty)
        assertTrue(row.cancelCreditUsed)
        val detail = NetworkClient.json.decodeFromString<HoldDetailResponse>("""{"groupId":23,"items":[{"holdId":41,"productId":17,"qty":2}]}""")
        assertEquals(23L, detail.groupId)
        assertEquals(41L, detail.items.single().holdId)
    }

    @Test
    fun recipeDifficultyRemainsNullableButDecodesKnownValues() {
        val recipe = NetworkClient.json.decodeFromString<RecipeSummaryResponse>("""{"difficulty":"HARD","cookTimeMinutes":35}""")
        assertEquals(RecipeSummaryResponse.Difficulty.HARD, recipe.difficulty)
        assertEquals(35, recipe.cookTimeMinutes)
        assertNull(NetworkClient.json.decodeFromString<RecipeSummaryResponse>("{}").difficulty)
        assertNull(NetworkClient.json.decodeFromString<RecipeSummaryResponse>("""{"difficulty":null}""").difficulty)
    }
}
