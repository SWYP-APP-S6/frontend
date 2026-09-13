package com.swyp.mangro.remote.auth

import com.swyp.mangro.core.network.NetworkClient
import com.swyp.mangro.core.network.readHttpError
import com.swyp.mangro.remote.auth.di.AuthServices
import com.swyp.mangro.remote.auth.model.LogoutRequest
import com.swyp.mangro.remote.auth.model.RefreshUserAuthKeyRequest
import com.swyp.mangro.remote.auth.model.RegisterUserRequest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthContractTest {
    @Test
    fun signupSendsDefaultConsentFieldsAndAccepts201() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(201).setBody("""{"status":201,"code":"OK","message":"ok","data":{"accessToken":"access","refreshToken":"refresh"}}"""))
            val service = AuthServices(NetworkClient.create(server.url("/").toString())).auth
            val response = service.registerUser(RegisterUserRequest(signupToken = "signup"))
            assertTrue(response.isSuccessful)
            assertEquals("access", response.body()?.data?.accessToken)
            val request = checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            assertEquals("POST", request.method)
            assertEquals("/auth/signup", request.path)
            val body = NetworkClient.json.parseToJsonElement(request.body.readUtf8()).jsonObject
            assertEquals("false", body["thirdPartyTermsAgreed"]?.jsonPrimitive?.content)
            assertEquals("signup", body["signupToken"]?.jsonPrimitive?.content)
            assertEquals(6, body.size)
        }
    }

    @Test
    fun refreshUsesBodyAndRetainsHttpErrorMetadata() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(429).addHeader("Retry-After", "30").setBody("""{"status":429,"code":"TOO_MANY_REQUESTS","message":"wait","fieldErrors":null,"retryAt":null}"""))
            val response = AuthServices(NetworkClient.create(server.url("/").toString())).auth
                .refreshUserAuthKey(RefreshUserAuthKeyRequest(refreshToken = "refresh"))
            assertFalse(response.isSuccessful)
            val error = checkNotNull(response.readHttpError())
            assertEquals("TOO_MANY_REQUESTS", error.code)
            assertEquals("30", error.retryAfter)
            assertEquals("/auth/refresh", server.takeRequest(5, TimeUnit.SECONDS)?.path)
        }
    }

    @Test
    fun voidEnvelopeAcceptsJsonNull() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"ok","data":null}"""))
            val response = AuthServices(NetworkClient.create(server.url("/").toString())).auth
                .logout(LogoutRequest(refreshToken = "refresh"))
            assertTrue(response.isSuccessful)
            assertEquals(200, response.body()?.status)
            org.junit.Assert.assertNull(response.body()?.data)
        }
    }

    @Test
    fun requestDefaultsAreEncoded() {
        val body = NetworkClient.json.encodeToString(RefreshUserAuthKeyRequest())
        assertEquals("""{"refreshToken":""}""", body)
    }
}
