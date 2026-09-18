package com.swyp.mangro.data.owner.product

import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.data.owner.product.repository.StockReconfirmationRepository
import com.swyp.mangro.remote.owner.service.ProductService
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StockReconfirmationRepositoryTest {
    private val server = MockWebServer()
    private val repository by lazy {
        val retrofit = NetworkModule.provideRetrofit(OkHttpClient(), NetworkModule.provideNetworkJson()).newBuilder().baseUrl(server.url("/")).build()
        StockReconfirmationRepository(retrofit.create(ProductService::class.java))
    }

    @After fun close() = server.shutdown()
    private fun enqueue(pending: Boolean = true) {
        server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":{"id":7,"name":"시금치 한 단","stockQty":4,"availableQty":1,"reconfirmPending":$pending,"stockEditable":true}}"""))
    }

    @Test fun usesLatestPhysicalStockAndSkipsAlreadyAnsweredRequests() = runTest {
        enqueue()
        val product = repository.fetch(7).getOrThrow()!!
        assertEquals(4, product.quantity)
        assertEquals("/owner/products/7", server.takeRequest().path)
        enqueue(false)
        assertNull(repository.fetch(7).getOrThrow())
    }

    @Test fun sendsBothAnswersToReconfirmationEndpointAndDoesNotRetryErrors() = runTest {
        for (confirmed in listOf(true, false)) {
            enqueue(false)
            repository.answer(7, confirmed).getOrThrow()
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/owner/products/7/stock-reconfirm", request.path)
            assertEquals("{\"confirmed\":$confirmed}", request.body.readUtf8())
        }
        server.enqueue(MockResponse().setResponseCode(409))
        assertTrue(repository.answer(7, true).isFailure)
        assertEquals(3, server.requestCount)
    }
}
