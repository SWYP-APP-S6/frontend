package com.swyp.mangro.feature.owner.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asSnapshot
import com.swyp.mangro.data.owner.product.model.HoldDetail
import com.swyp.mangro.data.owner.product.model.HoldItem
import com.swyp.mangro.data.owner.product.model.HoldPage
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedHold
import com.swyp.mangro.data.owner.product.paging.OwnerHoldPagingSource
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import com.swyp.mangro.feature.owner.product.screen.list.ProductListAction
import com.swyp.mangro.feature.owner.product.screen.list.ProductListFilter
import com.swyp.mangro.feature.owner.product.screen.list.ProductListTab
import com.swyp.mangro.feature.owner.product.screen.list.ProductListViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductListPagingTest {
    private val store = ViewModelStore()
    private val requests = mutableListOf<Pair<Int, HoldStatus?>>()
    private var writes = 0
    private val completion = CompletableDeferred<Result<HoldDetail>>()
    private val repository = object : OwnerProductRepository {
        private var source: OwnerHoldPagingSource? = null
        override fun pagedHolds(status: HoldStatus?, onPageLoaded: (HoldPage) -> Unit) = Pager(
            PagingConfig(pageSize = 100, initialLoadSize = 100, prefetchDistance = 5, enablePlaceholders = false),
        ) { OwnerHoldPagingSource(this, status, onPageLoaded).also { source = it } }.flow
        override fun refreshHolds() {
            source?.invalidate()
        }

        override fun fetchHolds(page: Int, status: HoldStatus?) = flow {
            requests += page to status
            val now = System.currentTimeMillis()
            val rows = (1..100).map { ManagedHold(page * 100L + it, 1, 1, "상품", "사용자", 1, now, now + 60000, status ?: HoldStatus.HOLDING) }
            emit(Result.success(HoldPage(rows, 300, page == 2, now, if (status == null) 300 else 200)))
        }
        override fun fetchProduct(id: Long) = error("unused")
        override fun updateStock(id: Long, quantity: Int) = error("unused")
        override fun fetchHold(id: Long) = error("unused")
        override fun markAsPickedUp(id: Long) = flow {
            writes++
            emit(completion.await())
        }
        override fun fetchCancellations() = error("unused")
        override fun cancelHolds(ids: Set<Long>) = error("unused")
    }

    @Before fun setup() = Dispatchers.setMain(StandardTestDispatcher())

    @After fun teardown() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun viewModel(tab: ProductListTab = ProductListTab.PICKUPS) = ProductListViewModel(SavedStateHandle(mapOf("store_tab" to tab.name)), repository).also { store.put("list", it) }

    @Test fun homeShortcutOverridesRestoredTabAndFilterAndQueriesMatchingStatus() = runTest {
        val handle = SavedStateHandle(mapOf("store_tab" to ProductListTab.PRODUCTS.name, "store_filter" to ProductListFilter.EXPIRED.name))
        val vm = ProductListViewModel(handle, repository).also { store.put("list", it) }
        vm.openPickups(ProductListFilter.COMPLETED)
        runCurrent()
        assertEquals(ProductListTab.PICKUPS, vm.uiState.value.tab)
        assertEquals(ProductListFilter.COMPLETED, vm.uiState.value.filter)
        vm.pickups.asSnapshot()
        assertEquals(listOf(0 to HoldStatus.COMPLETED), requests)
        vm.openPickups(ProductListFilter.ALL)
        runCurrent()
        assertEquals(ProductListFilter.ALL, vm.uiState.value.filter)
        vm.pickups.asSnapshot()
        assertEquals(listOf(0 to HoldStatus.COMPLETED, 0 to null), requests)
        assertEquals(ProductListTab.PICKUPS.name, handle.get<String>("store_tab"))
        assertEquals(ProductListFilter.ALL.name, handle.get<String>("store_filter"))
    }

    @Test fun initialCollectionLoadsOnePageAndScrollLoadsTheNext() = runTest {
        val vm = viewModel()
        assertEquals(100, vm.pickups.asSnapshot().size)
        assertEquals(listOf(0 to null), requests)
        val rows = vm.pickups.asSnapshot { scrollTo(105) }
        assertEquals(200, rows.size)
        assertEquals(listOf(0 to null, 1 to null), requests)
        assertEquals(300L, vm.uiState.value.filteredTotal)
    }

    @Test fun filterCreatesNewSourceStartingAtZero() = runTest {
        val vm = viewModel()
        vm.pickups.asSnapshot { scrollTo(105) }
        vm.handleAction(ProductListAction.FilterSelected(ProductListFilter.COMPLETED))
        runCurrent()
        val rows = vm.pickups.asSnapshot()
        assertEquals(100, rows.size)
        assertEquals(0 to HoldStatus.COMPLETED, requests.last())
        assertEquals(200L, vm.uiState.value.filteredTotal)
        assertEquals(300L, vm.uiState.value.totalHolds)
    }

    @Test fun refreshInvalidatesAndReloadsSource() = runTest {
        val vm = viewModel()
        vm.pickups.asSnapshot()
        vm.refresh()
        runCurrent()
        vm.pickups.asSnapshot()
        assertEquals(listOf(0 to null, 0 to null), requests)
    }

    @Test fun switchingTabsAndRecollectingRetainsLoadedPages() = runTest {
        val vm = viewModel()
        vm.pickups.asSnapshot { scrollTo(105) }
        vm.handleAction(ProductListAction.TabSelected(ProductListTab.PRODUCTS))
        runCurrent()
        assertEquals(200, vm.pickups.asSnapshot().size)
        vm.handleAction(ProductListAction.TabSelected(ProductListTab.PICKUPS))
        runCurrent()
        assertEquals(200, vm.pickups.asSnapshot().size)
        assertEquals(listOf(0 to null, 1 to null), requests)
    }

    @Test fun productTabLoadsHoldsCountBeforePickupTabIsSelected() = runTest {
        val vm = viewModel(ProductListTab.PRODUCTS)
        assertEquals(null, vm.uiState.value.totalHolds)
        assertEquals(100, vm.pickups.asSnapshot().size)
        assertEquals(300L, vm.uiState.value.totalHolds)
        vm.openPickups(ProductListFilter.ALL)
        runCurrent()
        assertEquals(100, vm.pickups.asSnapshot().size)
        assertEquals(300L, vm.uiState.value.filteredTotal)
        assertEquals(listOf(0 to null), requests)
    }

    @Test fun completionBlocksDuplicateWritesAndRefreshesPaging() = runTest {
        val vm = viewModel()
        val pickup = vm.pickups.asSnapshot().first()
        vm.handleAction(ProductListAction.PickupCompleteClicked(pickup))
        vm.handleAction(ProductListAction.PickupCompleteClicked(pickup))
        runCurrent()
        assertEquals(1, writes)
        assertTrue(vm.uiState.value.mutationInProgress)
        completion.complete(Result.success(HoldDetail(1, "사용자", "상점", HoldStatus.COMPLETED, 0, 1000, 1, 1, 100, listOf(HoldItem(1, 1, "상품", 1, 100, 100)))))
        runCurrent()
        vm.pickups.asSnapshot()
        assertEquals(listOf(0 to null, 0 to null), requests)
        assertEquals(false, vm.uiState.value.mutationInProgress)
    }
}
