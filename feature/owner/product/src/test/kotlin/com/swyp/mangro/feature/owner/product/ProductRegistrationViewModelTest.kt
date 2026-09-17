package com.swyp.mangro.feature.owner.product

import com.swyp.mangro.data.owner.product.model.ProductRegistration
import com.swyp.mangro.data.owner.product.model.RegisteredProduct
import com.swyp.mangro.data.owner.product.repository.ProductRepository
import com.swyp.mangro.data.owner.store.model.OwnerStore
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.data.owner.store.model.StoreRegistration
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.editor.ProductRegistrationViewModel
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
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
    private var response = Result.success(OwnerStore(9, "상점", listOf("BAKERY"), StoreApprovalStatus.APPROVED, "10:00:00", "21:00:00"))
    private var calls = 0
    private var registerCalls = 0
    private var expectedPickupEndAt = "2026-09-17T20:00+09:00"
    private var registrationResult = Result.success(RegisteredProduct(91, "채소", "https://example.test/photo", 2000, 1000, 3, 3, 0, 0, "2026-09-17T20:00:00+09:00"))
    private val products = object : ProductRepository {
        override fun register(product: ProductRegistration) = flow {
            registerCalls++
            assertTrue(product.ingredientTags.isEmpty())
            assertEquals("BAKERY", product.category)
            assertEquals(expectedPickupEndAt, product.pickupEndAt)
            emit(registrationResult)
        }
    }
    private val clock = Clock.fixed(Instant.parse("2026-09-17T01:00:00Z"), ZoneId.of("Asia/Seoul"))
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
        val vm = ProductRegistrationViewModel(repository, products, clock)
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
        assertEquals(StoreApprovalStatus.PENDING, vm.uiState.value.store?.status)
    }

    @Test fun approvedSubmissionIsDeliveredOnceDespiteDoubleTap() = runTest {
        val vm = ProductRegistrationViewModel(repository, products, clock)
        val saves = mutableListOf<OwnerProductModel>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.save.collect { saves.add(it) } }
        vm.refresh()
        advanceUntilIdle()
        repeat(2) { vm.submit(OwnerProductModel("1", "채소", emptyList(), 2000, 1000, 3, 3, pickupEndTime = "20:00")) }
        advanceUntilIdle()
        vm.submit(OwnerProductModel("1", "채소", emptyList(), 2000, 1000, 3, 3, pickupEndTime = "20:00"))
        advanceUntilIdle()
        assertEquals(1, saves.size)
        assertEquals("91", saves.single().id)
        assertEquals(1, registerCalls)
        assertEquals(2, calls)
    }

    @Test fun failedRegistrationKeepsEditorAndAllowsRetry() = runTest {
        val vm = ProductRegistrationViewModel(repository, products, clock)
        val saves = mutableListOf<OwnerProductModel>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.save.collect { saves.add(it) } }
        vm.refresh()
        advanceUntilIdle()
        val success = registrationResult
        registrationResult = Result.failure(IllegalStateException())
        val draft = OwnerProductModel("local", "채소", listOf("first", "second"), 2000, 1000, 3, 3, pickupEndTime = "20:00")
        vm.submit(draft)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.registrationFailed)
        assertFalse(vm.uiState.value.isSubmitting)
        assertTrue(vm.uiState.value.store?.canRegisterProduct == true)
        assertTrue(saves.isEmpty())
        vm.dismissError()
        registrationResult = success
        vm.submit(draft)
        advanceUntilIdle()
        assertEquals(2, registerCalls)
        assertEquals("91", saves.single().id)
    }

    @Test fun expiredPickupTimeDoesNotUploadOrRegister() = runTest {
        response = Result.success(response.getOrThrow().copy(businessCloseTime = "09:00:00"))
        val vm = ProductRegistrationViewModel(repository, products, clock)
        vm.refresh()
        advanceUntilIdle()
        vm.submit(OwnerProductModel("local", "채소", listOf("photo"), 2000, 1000, 3, 3, pickupEndTime = "09:00"))
        advanceUntilIdle()
        assertEquals(0, registerCalls)
        assertTrue(vm.uiState.value.registrationFailed)
        assertFalse(vm.uiState.value.isSubmitting)
    }

    @Test fun missingOrMultipleStoreCategoriesDoNotRegister() = runTest {
        for (categories in listOf(emptyList(), listOf("BAKERY", "FRUIT"))) {
            response = Result.success(response.getOrThrow().copy(categories = categories))
            val vm = ProductRegistrationViewModel(repository, products, clock)
            vm.refresh()
            advanceUntilIdle()
            vm.submit(OwnerProductModel("local", "상품", listOf("photo"), 2000, 1000, 3, 3, pickupEndTime = "20:00"))
            advanceUntilIdle()
            assertEquals(0, registerCalls)
            assertTrue(vm.uiState.value.registrationFailed)
        }
    }

    @Test fun submissionPreservesSelectedTimeWhenStoreClosingTimeChanges() = runTest {
        val vm = ProductRegistrationViewModel(repository, products, clock)
        vm.refresh()
        advanceUntilIdle()
        response = Result.success(response.getOrThrow().copy(businessCloseTime = "22:30:00"))
        expectedPickupEndAt = "2026-09-17T19:00+09:00"
        vm.submit(OwnerProductModel("local", "상품", listOf("photo"), 2000, 1000, 3, 3, pickupEndTime = "19:00"))
        advanceUntilIdle()
        assertEquals(2, calls)
        assertEquals(1, registerCalls)
        assertFalse(vm.uiState.value.registrationFailed)
    }
}
