package com.swyp.mangro.data.owner.product

import androidx.paging.cachedIn
import androidx.paging.testing.asSnapshot
import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.data.owner.product.impl.OwnerProductRepositoryImpl
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.OwnerProductFilter
import com.swyp.mangro.remote.owner.service.HoldService
import com.swyp.mangro.remote.owner.service.ProductService
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerProductRepositoryTest {
    private val server = MockWebServer()
    private val repository by lazy {
        val retrofit = NetworkModule.provideRetrofit(OkHttpClient(), NetworkModule.provideNetworkJson()).newBuilder().baseUrl(server.url("/")).build()
        OwnerProductRepositoryImpl(retrofit.create(ProductService::class.java), retrofit.create(HoldService::class.java))
    }

    @After fun close() = server.shutdown()
    private fun enqueue(data: String) {
        server.enqueue(MockResponse().setBody("""{"status":200,"code":"OK","data":$data}"""))
    }
    private val product = """{"id":7,"name":"복숭아","initialQty":10,"originalPrice":5000,"salePrice":4000,"stockQty":2,"availableQty":0,"activeHoldQty":3000000000,"shortfallQty":5,"completedQty":4000000000,"pickupEndAt":"2026-09-17T20:00:00+09:00","stockEditable":true}"""

    private fun holdPage(page: Int): String {
        val rows = (1..100).joinToString(",") { index ->
            """{"id":${page * 100 + index},"productId":7,"heldAt":"2026-09-17T18:00:00+09:00","expiresAt":"2026-09-17T18:15:00+09:00"}"""
        }
        return """{"serverTime":"2026-09-17T18:00:00+09:00","counts":{"all":300},"holds":{"content":[$rows],"last":${page == 1},"totalElements":200}}"""
    }

    @Test fun repositoryPagerLoadsOnePageThenAppendsOnDemand() = runTest {
        enqueue(holdPage(0))
        enqueue(holdPage(1))
        var filteredTotal = 0L
        val pages = repository.pagedHolds(HoldStatus.COMPLETED) { filteredTotal = it.filteredTotal }.cachedIn(backgroundScope)
        assertEquals(100, pages.asSnapshot().size)
        assertEquals(1, server.requestCount)
        assertEquals("/owner/holds?status=COMPLETED&page=0&size=100", server.takeRequest().path)
        assertEquals(200L, filteredTotal)
        assertEquals(200, pages.asSnapshot { scrollTo(105) }.size)
        assertEquals("/owner/holds?status=COMPLETED&page=1&size=100", server.takeRequest().path)
        assertEquals(2, server.requestCount)
    }

    @Test fun repositoryRefreshInvalidatesActivePagingSource() = runTest {
        repeat(2) { enqueue(holdPage(0)) }
        val pages = repository.pagedHolds(null) {}.cachedIn(backgroundScope)
        pages.asSnapshot()
        repository.refreshHolds()
        pages.asSnapshot()
        assertEquals(2, server.requestCount)
        repeat(2) { assertEquals("/owner/holds?page=0&size=100", server.takeRequest().path) }
    }

    @Test fun eachProductFilterIsSentToServer() = runTest {
        OwnerProductFilter.entries.forEach { filter ->
            enqueue("""{"products":{"content":[],"last":true,"totalElements":0}}""")
            repository.fetchProducts(0, filter).single().getOrThrow()
            assertEquals("/owner/products?filter=${filter.name}&page=0&size=20", server.takeRequest().path)
        }
    }

    @Test fun productPagerUsesSelectedFilterAndPublishesServerTotal() = runTest {
        enqueue("""{"products":{"content":[],"last":true,"totalElements":2}}""")
        var total = -1L
        val pages = repository.pagedProducts(OwnerProductFilter.SOLD_OUT) { total = it.total }.cachedIn(backgroundScope)

        assertTrue(pages.asSnapshot().isEmpty())
        assertEquals("/owner/products?filter=SOLD_OUT&page=0&size=20", server.takeRequest().path)
        assertEquals(2L, total)
    }

    @Test fun productDetailUsesProductIdEndpoint() = runTest {
        enqueue(product.dropLast(1) + """, "ingredientTags":[12]}""")
        val result = repository.fetchProduct(7).single().getOrThrow()
        assertEquals(7L, result.id)
        assertEquals(setOf(12), result.ingredientTags)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/owner/products/7", request.path)
    }

    @Test fun productListPreservesShortfallCustomerCount() = runTest {
        enqueue(
            """{"products":{"content":[{"id":7,"name":"복숭아","shortfallCustomerCount":3}],"totalElements":1,"last":true}}""",
        )
        val result = repository.fetchProducts(0).single().getOrThrow()
        assertEquals(3L, result.products.single().shortfallCustomerCount)
        assertEquals("/owner/products?filter=ALL&page=0&size=20", server.takeRequest().path)
    }

    @Test fun cancellationCandidatesUseDedicatedEndpoint() = runTest {
        enqueue("""{"noticeMessage":"서버 안내","suggestedCancelCount":0,"products":[]}""")
        assertEquals("서버 안내", repository.fetchCancellations().single().getOrThrow().notice)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/owner/holds/cancel-candidates", request.path)
    }

    @Test fun stockUpdateUsesPhysicalStockAndReturnsServerAllocation() = runTest {
        enqueue(product)
        val result = repository.updateStock(7, 2).single().getOrThrow()
        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/owner/products/7/stock", request.path)
        assertEquals("{\"stockQty\":2}", request.body.readUtf8())
        assertEquals(2, result.stockQuantity)
        assertEquals(0, result.availableQuantity)
        assertEquals(5, result.shortfallQuantity)
        assertEquals(3000000000L, result.activeHoldQuantity)
        assertEquals(4000000000L, result.completedQuantity)
    }

    @Test fun pagedHoldsPreserveServerClockAndCounts() = runTest {
        enqueue("""{"serverTime":"2026-09-17T18:00:00+09:00","counts":{"all":3000000000},"holds":{"content":[],"last":false,"page":2,"totalElements":120}}""")
        val result = repository.fetchHolds(2).single().getOrThrow()
        assertEquals("/owner/holds?page=2&size=100", server.takeRequest().path)
        assertEquals(3000000000L, result.total)
        assertEquals(120L, result.filteredTotal)
        assertFalse(result.last)
        assertTrue(result.serverTime > 0)
    }

    @Test fun eachHoldFilterIsSentToServer() = runTest {
        HoldStatus.entries.forEach { status ->
            enqueue("""{"serverTime":"2026-09-17T18:00:00+09:00","counts":{"all":0},"holds":{"content":[],"last":true}}""")
            repository.fetchHolds(0, status).single().getOrThrow()
            assertEquals("/owner/holds?status=${status.name}&page=0&size=100", server.takeRequest().path)
        }
    }

    @Test fun cancellationUsesExplicitSelectedIdsAndReturnedCandidates() = runTest {
        enqueue("""{"noticeMessage":"서버 안내","suggestedCancelCount":1,"products":[{"productId":7,"productName":"복숭아","shortfallQty":2,"holds":[{"holdId":9,"heldOrder":5,"heldAt":"2026-09-17T18:00:00+09:00","nickname":"손님","qty":2,"suggested":true}]}]}""")
        val result = repository.cancelHolds(setOf(8)).single().getOrThrow()
        val request = server.takeRequest()
        assertEquals("/owner/holds/cancel", request.path)
        assertEquals("{\"holdIds\":[8]}", request.body.readUtf8())
        assertEquals("서버 안내", result.notice)
        assertEquals(5, result.products.single().candidates.single().order)
        assertTrue(result.products.single().candidates.single().suggested)
    }

    @Test fun errorsAndInvalidRequestsNeverBecomeSuccessfulEmptyData() = runTest {
        server.enqueue(MockResponse().setResponseCode(409))
        assertTrue(repository.cancelHolds(setOf(9)).single().isFailure)
        assertTrue(repository.cancelHolds(emptySet()).single().isFailure)
        assertTrue(repository.cancelHolds((1L..101L).toSet()).single().isFailure)
        assertTrue(repository.updateStock(7, 10000).single().isFailure)
        enqueue("null")
        assertTrue(repository.fetchProduct(7).single().isFailure)
        assertEquals(2, server.requestCount)
    }

    @Test fun groupedDetailPreservesEveryItemAndServerTotal() = runTest {
        enqueue("""{"groupId":4,"status":"COMPLETED","nickname":"손님","storeName":"가게","totalPrice":9000,"heldAt":"2026-09-17T18:00:00+09:00","expiresAt":"2026-09-17T18:15:00+09:00","serverTime":"2026-09-17T18:05:00+09:00","items":[{"holdId":8,"productId":7,"qty":1,"unitPrice":3000,"lineTotal":3000},{"holdId":9,"productId":2,"qty":2,"unitPrice":3000,"lineTotal":6000}]}""")
        val result = repository.markAsPickedUp(8).single().getOrThrow()
        assertEquals("/owner/holds/8/complete", server.takeRequest().path)
        assertEquals(2, result.items.size)
        assertEquals(9000, result.totalPrice)
    }
}
