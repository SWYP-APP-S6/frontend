package com.swyp.mangro.data.owner.home

import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.data.owner.home.impl.OwnerHomeRepositoryImpl
import com.swyp.mangro.remote.owner.service.HoldService
import com.swyp.mangro.remote.owner.service.HomeService
import java.time.Instant
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerHomeRepositoryTest {
    private val server = MockWebServer()
    private val repository by lazy {
        val retrofit = NetworkModule.provideRetrofit(OkHttpClient(), NetworkModule.provideNetworkJson()).newBuilder().baseUrl(server.url("/")).build()
        OwnerHomeRepositoryImpl(retrofit.create(HomeService::class.java), retrofit.create(HoldService::class.java))
    }

    @After fun close() = server.shutdown()

    private fun enqueue(data: String) {
        server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":$data}"""))
    }

    @Test fun mapsServerCountsShortfallAndAbsoluteExpiryWithoutInferringRegistration() = runTest {
        enqueue(
            """{
            "store":{"id":9,"status":"APPROVED"},"hasRegisteredProduct":true,
            "summary":{"upcomingVisitCount":2,"completedTodayCount":3000000000,"onSaleProductCount":7},
            "unreadNotificationCount":5,"reconfirmPendingCount":4,
            "issues":{"expiredTodayCount":3,"productsShortOfStock":1,"shortfallQty":2},
            "upcomingVisits":[{"holdId":17,"nickname":"손님","summary":"채소","totalQty":3,"expiresAt":"2026-09-17T18:30:00+09:00"}],
            "products":[{"id":20,"name":"채소","availableQty":6,"activeHoldQty":3000000000,"shortfallQty":2,"shortfallCustomerCount":3}]
        }""",
        )
        val home = repository.fetchHome().single().getOrThrow()
        assertEquals("/owner/home", server.takeRequest().path)
        assertTrue(home.hasRegisteredProduct)
        assertEquals(3000000000L, home.completedTodayCount)
        assertEquals(7, home.onSaleProductCount)
        assertEquals(2, home.products.single().shortfallQty)
        assertEquals(3L, home.products.single().shortfallCustomerCount)
        assertEquals(3000000000L, home.products.single().activeHoldQty)
        assertEquals(Instant.parse("2026-09-17T09:30:00Z").toEpochMilli(), home.upcomingVisits.single().expiresAtMillis)
        enqueue("""{"store":{"id":9},"hasRegisteredProduct":true,"products":[]}""")
        assertTrue(repository.fetchHome().single().getOrThrow().hasRegisteredProduct)
    }

    @Test fun badResponseNeverBecomesWelcomeOrInventedDeadline() = runTest {
        for (body in listOf("null", "{}", """{"store":{"id":9},"upcomingVisits":[{"holdId":1,"expiresAt":"bad"}]}""")) {
            enqueue(body)
            assertTrue(repository.fetchHome().single().isFailure)
        }
        server.enqueue(MockResponse().setResponseCode(403))
        assertTrue(repository.fetchHome().single().isFailure)
    }

    @Test fun completesExactlyRequestedHoldAndRejectsNonCompletedResponse() = runTest {
        enqueue("""{"groupId":17,"status":"COMPLETED"}""")
        assertTrue(repository.markAsPickedUp(17).single().isSuccess)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/owner/holds/17/complete", request.path)
        enqueue("""{"groupId":17,"status":"HOLDING"}""")
        assertTrue(repository.markAsPickedUp(17).single().isFailure)
        assertTrue(repository.markAsPickedUp(0).single().isFailure)
        assertEquals(2, server.requestCount)
    }
}
