package com.swyp.mangro.feature.owner.product

import com.swyp.mangro.data.owner.store.model.OwnerStore
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.data.owner.store.model.StoreRegistration
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.editor.ProductRegistrationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductRegistrationViewModelTest {
    private var response = Result.success(OwnerStore(9, "상점", emptyList(), StoreApprovalStatus.APPROVED, "10:00:00", "21:00:00"))
    private var calls = 0
    private val repository = object : StoreRepository {
        override fun fetchMyStore() = flow {
            calls++
            emit(response)
        }
        override fun register(registration: StoreRegistration): Flow<Result<Unit>> = error("unused")
    }

    @Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun restoredEntryChecksServerAndSubmissionRechecksApproval() = runTest {
        val vm = ProductRegistrationViewModel(repository)
        val saves = mutableListOf<OwnerProductModel>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.save.collect { saves.add(it) } }
        assertTrue(vm.uiState.value.isChecking)
        vm.refresh()
        advanceUntilIdle()
        assertEquals("10:00:00", vm.uiState.value.store?.businessOpenTime)
        assertTrue(vm.uiState.value.store?.canRegisterProduct == true)
        response = Result.success(response.getOrThrow().copy(status = StoreApprovalStatus.PENDING))
        vm.submit(OwnerProductModel("1", "채소", emptyList(), 2000, 1000, 3, 3, pickupEndTime = "20:00"))
        advanceUntilIdle()
        assertEquals(2, calls)
        assertTrue(saves.isEmpty())
        assertFalse(vm.uiState.value.store?.canRegisterProduct == true)
        response = Result.failure(IllegalStateException())
        vm.refresh()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.checkFailed)
        assertEquals(null, vm.uiState.value.store)
    }

    @Test fun approvedSubmissionIsDeliveredOnceDespiteDoubleTap() = runTest {
        val vm = ProductRegistrationViewModel(repository)
        val saves = mutableListOf<OwnerProductModel>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.save.collect { saves.add(it) } }
        vm.refresh()
        advanceUntilIdle()
        repeat(2) { vm.submit(OwnerProductModel("1", "채소", emptyList(), 2000, 1000, 3, 3, pickupEndTime = "20:00")) }
        advanceUntilIdle()
        vm.submit(OwnerProductModel("1", "채소", emptyList(), 2000, 1000, 3, 3, pickupEndTime = "20:00"))
        advanceUntilIdle()
        assertEquals(1, saves.size)
        assertEquals(2, calls)
    }
}
