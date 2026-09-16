package com.swyp.mangro.data.owner.notification

import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.core.network.interceptor.AuthorizationInterceptor
import com.swyp.mangro.remote.user.service.NotificationService
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerNotificationRepositoryTest {
    private val server = MockWebServer()
    private val repository by lazy {
        val client = OkHttpClient.Builder().addInterceptor(AuthorizationInterceptor(server.url("/")) { "owner-session" }).build()
        val retrofit = NetworkModule.provideRetrofit(client, NetworkModule.provideNetworkJson()).newBuilder().baseUrl(server.url("/")).build()
        OwnerNotificationRepository(retrofit.create(NotificationService::class.java))
    }

    @After fun close() = server.shutdown()

    @Test fun marksOnlyTheOpenedNotificationAsRead() = runTest {
        server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":{"id":1024,"type":"NEW_HOLD_RECEIVED","title":"제목","body":"본문","notifiedAt":"2026-09-17T08:00:00+09:00"}}"""))
        assertTrue(repository.markAsRead(1024).single().isSuccess)
        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/notifications/1024/read", request.path)
        assertTrue(repository.markAsRead(0).single().isFailure)
        assertEquals(1, server.requestCount)
    }

    @Test fun registersAndroidTokenWithOwnerAuthorization() = runTest {
        server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":null}"""))
        assertTrue(repository.registerToken("test-fcm-token").single().isSuccess)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/notifications/device-tokens", request.path)
        assertEquals("Bearer owner-session", request.getHeader("Authorization"))
        assertEquals("""{"fcmToken":"test-fcm-token","platform":"ANDROID"}""", request.body.readUtf8())
    }

    @Test fun unregistersOnlyTheCurrentDeviceToken() = runTest {
        server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":null}"""))
        assertTrue(repository.deleteToken("test-fcm-token").single().isSuccess)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/notifications/device-tokens", request.path)
        assertEquals("""{"fcmToken":"test-fcm-token"}""", request.body.readUtf8())
    }

    @Test fun failedRegistrationIsNotSuccessAndBlankTokenDoesNotCallServer() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))
        assertTrue(repository.registerToken("test").single().isFailure)
        assertTrue(repository.registerToken("").single().isFailure)
        assertEquals(1, server.requestCount)
    }
}
