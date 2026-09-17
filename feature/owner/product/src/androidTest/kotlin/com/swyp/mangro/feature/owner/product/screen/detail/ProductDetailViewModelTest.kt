package com.swyp.mangro.feature.owner.product.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swyp.mangro.feature.owner.product.ManagementFakeRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProductDetailViewModelTest {
    @Test fun initLoadsWithoutRouteAndRetryDoesNothingAfterSuccess() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val gate = CompletableDeferred<Unit>()
            val repo = ManagementFakeRepository().apply { productReadGate = gate }
            val vm = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to "7")), repo)
            assertTrue(vm.uiState.value.isLoading)
            assertFalse(vm.uiState.value.hasError)
            gate.complete(Unit)
            runCurrent()
            assertEquals(1, repo.productReads)
            assertFalse(vm.uiState.value.isLoading)
            vm.handleAction(ProductDetailAction.RetryClicked)
            runCurrent()
            assertEquals(1, repo.productReads)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun failedInitialLoadWaitsForExplicitRetryAndIgnoresRetryWhileLoading() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val initialGate = CompletableDeferred<Unit>()
            val repo = ManagementFakeRepository().apply {
                failed = true
                productReadGate = initialGate
            }
            val vm = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to "7")), repo)
            assertFalse(vm.uiState.value.hasError)
            initialGate.complete(Unit)
            runCurrent()
            assertTrue(vm.uiState.value.hasError)
            runCurrent()
            assertEquals(1, repo.productReads)
            repo.failed = false
            val gate = CompletableDeferred<Unit>()
            repo.productReadGate = gate
            vm.handleAction(ProductDetailAction.RetryClicked)
            runCurrent()
            vm.handleAction(ProductDetailAction.RetryClicked)
            runCurrent()
            assertEquals(2, repo.productReads)
            assertTrue(vm.uiState.value.isLoading)
            assertFalse(vm.uiState.value.hasError)
            gate.complete(Unit)
            runCurrent()
            assertEquals(2, repo.productReads)
            assertFalse(vm.uiState.value.hasError)
            assertEquals(7L.toString(), vm.uiState.value.product?.id)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun stockSaveRequiresConfirmationAndUsesReturnedShortage() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = ManagementFakeRepository()
            val vm = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to "7")), repo)
            runCurrent()
            vm.handleAction(ProductDetailAction.QuantityChanged(1))
            vm.handleAction(ProductDetailAction.SaveClicked)
            assertTrue(vm.uiState.value.showSaveConfirmation)
            vm.handleAction(ProductDetailAction.SaveConfirmClicked)
            vm.handleAction(ProductDetailAction.SaveConfirmClicked)
            runCurrent()
            assertEquals(1, repo.writes)
            assertEquals(2, vm.uiState.value.savedShortage)
            assertTrue(vm.uiState.value.showSaved)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun failedSaveKeepsServerStockAndAllowsExplicitRetry() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = ManagementFakeRepository()
            val vm = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to "7")), repo)
            runCurrent()
            repo.failed = true
            vm.handleAction(ProductDetailAction.QuantityChanged(4))
            vm.handleAction(ProductDetailAction.SaveClicked)
            runCurrent()
            assertTrue(vm.uiState.value.hasError)
            assertFalse(vm.uiState.value.showSaved)
            assertEquals(5, vm.uiState.value.product?.remainingQuantity)
            repo.failed = false
            vm.handleAction(ProductDetailAction.RetryClicked)
            runCurrent()
            assertFalse(vm.uiState.value.hasError)
            assertEquals(5, vm.uiState.value.quantity)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun serverEditPermissionAndMaximumQuantityAreEnforced() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = ManagementFakeRepository().also { it.product = it.product.copy(stockEditable = false) }
            val vm = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to "7")), repo)
            runCurrent()
            vm.handleAction(ProductDetailAction.QuantityChanged(10000))
            assertEquals(5, vm.uiState.value.quantity)
            vm.handleAction(ProductDetailAction.QuantityChanged(4))
            vm.handleAction(ProductDetailAction.SaveClicked)
            runCurrent()
            assertFalse(vm.uiState.value.canSave)
            assertEquals(0, repo.writes)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
