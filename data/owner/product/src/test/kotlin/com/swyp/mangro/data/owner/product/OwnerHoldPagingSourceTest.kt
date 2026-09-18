package com.swyp.mangro.data.owner.product

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.swyp.mangro.data.owner.product.model.HoldPage
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedHold
import com.swyp.mangro.data.owner.product.model.OwnerProductFilter
import com.swyp.mangro.data.owner.product.model.ProductPage
import com.swyp.mangro.data.owner.product.paging.OwnerHoldPagingSource
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerHoldPagingSourceTest {
    private val requests = mutableListOf<Pair<Int, HoldStatus?>>()
    private var failure: Exception? = null
    private var empty = false
    private val repository = object : OwnerProductRepository {
        override fun pagedProducts(filter: OwnerProductFilter, onPageLoaded: (ProductPage) -> Unit) = error("unused")
        override fun refreshProducts() = Unit
        override fun fetchProducts(page: Int, filter: OwnerProductFilter) = error("unused")
        override fun pagedHolds(status: HoldStatus?, onPageLoaded: (HoldPage) -> Unit) = error("unused")
        override fun refreshHolds() = Unit
        override fun fetchHolds(page: Int, status: HoldStatus?) = flow {
            requests += page to status
            failure?.let { throw it }
            emit(Result.success(HoldPage(if (empty) emptyList() else listOf(hold(page.toLong())), 2, page == 1, 1234)))
        }
        override fun fetchProduct(id: Long) = error("unused")
        override fun updateStock(id: Long, quantity: Int) = error("unused")
        override fun fetchHold(id: Long) = error("unused")
        override fun markAsPickedUp(id: Long) = error("unused")
        override fun fetchCancellations() = error("unused")
        override fun cancelHolds(ids: Set<Long>) = error("unused")
    }

    @Test fun loadsOnlyRequestedPageAndStopsAtLastPage() = runTest {
        val source = OwnerHoldPagingSource(repository, HoldStatus.COMPLETED)
        val first = source.load(PagingSource.LoadParams.Refresh(null, 100, false)) as PagingSource.LoadResult.Page
        assertEquals(listOf(0 to HoldStatus.COMPLETED), requests)
        assertNull(first.prevKey)
        assertEquals(1, first.nextKey)
        val last = source.load(PagingSource.LoadParams.Append(1, 100, false)) as PagingSource.LoadResult.Page
        assertEquals(0, last.prevKey)
        assertNull(last.nextKey)
        assertEquals(listOf(0 to HoldStatus.COMPLETED, 1 to HoldStatus.COMPLETED), requests)
    }

    @Test fun failedAppendCanRetrySameKey() = runTest {
        val source = OwnerHoldPagingSource(repository, null)
        failure = IllegalStateException("network")
        assertTrue(source.load(PagingSource.LoadParams.Append(1, 100, false)) is PagingSource.LoadResult.Error)
        failure = null
        assertTrue(source.load(PagingSource.LoadParams.Append(1, 100, false)) is PagingSource.LoadResult.Page)
        assertEquals(listOf(1 to null, 1 to null), requests)
    }

    @Test fun emptyNonFinalPageIsAnErrorRatherThanInfiniteAppend() = runTest {
        empty = true
        val source = OwnerHoldPagingSource(repository, null)
        assertTrue(source.load(PagingSource.LoadParams.Refresh(null, 100, false)) is PagingSource.LoadResult.Error)
        assertTrue(source.load(PagingSource.LoadParams.Refresh(1, 100, false)) is PagingSource.LoadResult.Page)
    }

    @Test fun cancellationIsNotConvertedToLoadError() = runTest {
        failure = CancellationException()
        try {
            OwnerHoldPagingSource(repository, null).load(PagingSource.LoadParams.Refresh(null, 100, false))
            throw AssertionError("Cancellation was swallowed")
        } catch (_: CancellationException) {
            assertEquals(1, requests.size)
        }
    }

    @Test fun invalidatedLoadDoesNotPublishMetadata() = runTest {
        var callbacks = 0
        val source = OwnerHoldPagingSource(repository, null) { callbacks++ }
        source.invalidate()
        assertTrue(source.load(PagingSource.LoadParams.Refresh(null, 100, false)) is PagingSource.LoadResult.Invalid)
        assertEquals(0, callbacks)
    }

    @Test fun refreshKeyUsesPageNearestAnchor() {
        val source = OwnerHoldPagingSource(repository, null)
        val pages = listOf(PagingSource.LoadResult.Page(listOf(hold(1)), 2, 4))
        assertEquals(3, source.getRefreshKey(PagingState(pages, 0, PagingConfig(100), 0)))
        assertNull(source.getRefreshKey(PagingState(emptyList(), null, PagingConfig(100), 0)))
    }

    private fun hold(id: Long) = ManagedHold(id, 1, 1, "상품", "사용자", 1, 0, 9999, HoldStatus.HOLDING)
}
