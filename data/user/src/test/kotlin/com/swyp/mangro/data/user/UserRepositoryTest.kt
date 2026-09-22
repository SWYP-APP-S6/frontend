package com.swyp.mangro.data.user

import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.data.user.impl.UserRepositoryImpl
import com.swyp.mangro.remote.user.service.UserService
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException

class UserRepositoryTest {
    private val server = MockWebServer()
    private val repository by lazy {
        val retrofit = NetworkModule.provideRetrofit(OkHttpClient(), NetworkModule.provideNetworkJson()).newBuilder().baseUrl(server.url("/")).build()
        UserRepositoryImpl(retrofit.create(UserService::class.java))
    }

    @After fun close() = server.shutdown()

    @Test fun fetchesOwnUserForBothRolesSeparatelyFromStore() = runTest {
        for (role in listOf("OWNER", "CONSUMER")) {
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":{"id":7,"role":"$role","nickname":"사용자","phone":null,"marketingOptIn":true}}"""))
            val profile = repository.fetchMe().single().getOrThrow()
            assertEquals("/users/me", server.takeRequest().path)
            assertEquals(7L, profile.id)
            assertEquals(role, profile.role)
            assertEquals("사용자", profile.nickname)
            assertEquals(null, profile.phone)
            assertTrue(profile.marketingOptIn)
        }
    }

    @Test fun nullMissingUserAndHttpErrorsRemainFailures() = runTest {
        for (body in listOf("null", "{}")) {
            server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":$body}"""))
            assertTrue(repository.fetchMe().single().isFailure)
        }
        server.enqueue(MockResponse().setResponseCode(401))
        assertTrue(repository.fetchMe().single().isFailure)
    }

    @Test fun withdrawsCurrentUserWithDeleteRequest() = runTest {
        server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","message":"탈퇴했습니다.","data":null}"""))

        repository.withdrawUser().single()

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/users/me", request.path)
    }

    @Test fun withdrawHttpErrorIsNotReportedAsSuccess() = runTest {
        server.enqueue(MockResponse().setResponseCode(409).setBody("""{"status":409,"code":"STORE_HOLDING_HOLDS_REMAIN","message":"미처리 찜이 있습니다."}"""))

        val failure = runCatching { repository.withdrawUser().single() }.exceptionOrNull()

        assertTrue(failure is HttpException)
        assertEquals(409, (failure as HttpException).code())
    }
}
