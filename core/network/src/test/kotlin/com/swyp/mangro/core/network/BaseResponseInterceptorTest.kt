package com.swyp.mangro.core.network

import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.core.network.exception.BaseResponseException
import com.swyp.mangro.core.network.interceptor.UnwrapBaseResponse
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call
import retrofit2.http.POST

class BaseResponseInterceptorTest {
    interface Service {
        @POST("auth/signup")
        @UnwrapBaseResponse(allowNullData = false)
        fun signup(): Call<JsonObject>
    }

    private fun service(server: MockWebServer): Service = NetworkModule.provideRetrofit(OkHttpClient(), NetworkModule.provideNetworkJson())
        .newBuilder().baseUrl(server.url("/")).build().create(Service::class.java)

    @Test fun createdResponseUnwrapsSignupData() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(201).setBody("""{"status":201,"code":"CREATED","message":"생성했습니다.","data":{"accessToken":"test-access","refreshToken":"test-refresh"}}"""))
            val response = service(server).signup().execute()
            assertTrue(response.isSuccessful)
            assertEquals(201, response.code())
            assertEquals("test-access", response.body()?.get("accessToken")?.jsonPrimitive?.content)
            assertEquals("test-refresh", response.body()?.get("refreshToken")?.jsonPrimitive?.content)
        }
    }

    @Test(expected = BaseResponseException::class)
    fun errorCodeWithSuccessfulHttpStatusStillFails() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(201).setBody("""{"status":201,"code":"ERROR","data":{}}"""))
            service(server).signup().execute()
        }
    }
}
