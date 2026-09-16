package com.swyp.mangro.feature.owner.home

import com.swyp.mangro.data.owner.home.model.OwnerHome
import com.swyp.mangro.data.owner.home.model.OwnerHomeProduct
import com.swyp.mangro.data.owner.home.model.OwnerHomeVisit
import com.swyp.mangro.data.owner.home.repository.OwnerHomeRepository
import com.swyp.mangro.data.owner.store.model.OwnerStore
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.data.owner.store.model.StoreRegistration
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.data.user.model.UserProfile
import com.swyp.mangro.data.user.repository.UserRepository
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeAction
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeEvent
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OwnerHomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private var profile = Result.success(UserProfile(7, "OWNER", "점주", null, false, "", ""))
    private var store = Result.success(OwnerStore(9, "서버 상점", listOf("FRUIT"), StoreApprovalStatus.APPROVED, "09:00:00", "20:00:00"))
    private var home = Result.success(OwnerHome(9, "APPROVED", true, 2, 8, 11, 7, 3, 2, 4, 5, emptyList(), emptyList()))
    private var userCalls = 0
    private var storeCalls = 0
    private var homeCalls = 0
    private var completeCalls = 0
    private val completion = CompletableDeferred<Result<Unit>>()
    private val userRepository = object : UserRepository {
        override fun fetchMe() = flow {
            userCalls++
            emit(profile)
        }
    }
    private val storeRepository = object : StoreRepository {
        override fun fetchMyStore() = flow {
            storeCalls++
            emit(store)
        }
        override fun register(registration: StoreRegistration): Flow<Result<Unit>> = error("unused")
    }
    private val homeRepository = object : OwnerHomeRepository {
        override fun fetchHome() = flow {
            homeCalls++
            emit(home)
        }
        override fun markAsPickedUp(holdId: Long) = flow {
            completeCalls++
            emit(completion.await())
        }
    }
    private fun viewModel() = OwnerHomeViewModel(userRepository, storeRepository, homeRepository)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun fetchesAllThreeResourcesAndKeepsServerEmptyInventoryRegistrationFlag() = runTest {
        val vm = viewModel()
        assertTrue(vm.uiState.value.isLoading)
        vm.refresh()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(listOf(1, 1, 1), listOf(userCalls, storeCalls, homeCalls))
        assertEquals(7L, state.profile?.id)
        assertEquals("서버 상점", state.storeName)
        assertEquals("과일", state.storeCategory)
        assertTrue(state.hasRegisteredProduct)
        assertTrue(state.products.isEmpty())
        assertEquals(11, state.sellingCount)
        assertEquals(8L, state.completedPickupCount)
        assertTrue(state.canRegisterProduct)
        assertFalse(state.hasNewPickup)
        assertFalse(state.attentionAvailable)
        assertEquals(0, state.cancellationRequiredCount)
        vm.handleAction(OwnerHomeAction.RegisterProduct)
        assertEquals(OwnerHomeEvent.NavigateToRegisterProduct, vm.event.first())
    }

    @Test fun pendingRejectedUnknownFailedAndMismatchedStoreNeverEnableRegistration() = runTest {
        val vm = viewModel()
        for (status in StoreApprovalStatus.entries.filter { it != StoreApprovalStatus.APPROVED }) {
            store = Result.success(store.getOrThrow().copy(status = status))
            vm.refresh()
            advanceUntilIdle()
            assertFalse(vm.uiState.value.canRegisterProduct)
            vm.handleAction(OwnerHomeAction.RegisterProduct)
            assertTrue(vm.event.first() is OwnerHomeEvent.ShowMessage)
        }
        store = Result.success(store.getOrThrow().copy(status = StoreApprovalStatus.APPROVED))
        profile = Result.failure(IllegalStateException())
        vm.refresh()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.canRegisterProduct)
        profile = Result.success(UserProfile(7, "OWNER", "점주", null, false, "", ""))
        home = Result.success(home.getOrThrow().copy(storeId = 99))
        vm.refresh()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.canRegisterProduct)
    }

    @Test fun usesShortfallFieldNotDifferenceAndRetriesFailedHome() = runTest {
        val vm = viewModel()
        val loaded = home.getOrThrow().copy(products = listOf(OwnerHomeProduct(1, "채소", "", 2000, 6, 4, 2, "VEGETABLE", "ON_SALE", false)))
        home = Result.failure(IllegalStateException())
        vm.refresh()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)
        home = Result.success(loaded)
        vm.handleAction(OwnerHomeAction.Refresh)
        advanceUntilIdle()
        assertEquals(null, vm.uiState.value.errorMessage)
        assertEquals(2, vm.uiState.value.products.single().shortfallQty)
    }

    @Test fun pickupSubmissionIsDeduplicatedAndSuccessRefreshesAllData() = runTest {
        home = Result.success(home.getOrThrow().copy(upcomingVisits = listOf(OwnerHomeVisit(17, "손님", "채소", 2, Long.MAX_VALUE))))
        val vm = viewModel()
        vm.refresh()
        advanceUntilIdle()
        repeat(2) { vm.handleAction(OwnerHomeAction.MarkAsPickedUp("17")) }
        runCurrent()
        assertEquals(1, completeCalls)
        assertTrue("17" in vm.uiState.value.completingPickupIds)
        home = Result.success(home.getOrThrow().copy(upcomingVisits = emptyList()))
        completion.complete(Result.success(Unit))
        advanceUntilIdle()
        assertEquals(2, homeCalls)
        assertEquals(2, userCalls)
        assertTrue(vm.uiState.value.visitors.isEmpty())
        assertTrue(vm.uiState.value.completingPickupIds.isEmpty())
    }
}
