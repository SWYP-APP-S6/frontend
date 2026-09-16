package com.swyp.mangro.feature.owner.product.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swyp.mangro.feature.owner.product.ManagementFakeRepository
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
    @Test fun stockSaveRequiresConfirmationAndUsesReturnedShortage() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = ManagementFakeRepository()
            val vm = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to "7")), repo)
            vm.refresh()
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
            vm.refresh()
            runCurrent()
            repo.failed = true
            vm.handleAction(ProductDetailAction.QuantityChanged(4))
            vm.handleAction(ProductDetailAction.SaveClicked)
            runCurrent()
            assertTrue(vm.uiState.value.hasError)
            assertFalse(vm.uiState.value.showSaved)
            assertEquals(5, vm.uiState.value.product?.remainingQuantity)
            repo.failed = false
            vm.refresh()
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
            vm.refresh()
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
