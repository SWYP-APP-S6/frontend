package com.swyp.mangro.feature.owner.product.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductDetailViewModelTest {
    private val product = OwnerProductModel("peach", "Peach", emptyList(), 10000, 4000, 10, 5, 3, 2, "20:00")

    @Test
    fun unchangedAndNegativeQuantityCannotSave() = runTest {
        val model = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to product.id)))
        model.updateProducts(listOf(product))
        model.handleAction(ProductDetailAction.QuantityChanged(-1))
        model.handleAction(ProductDetailAction.SaveClicked)
        assertEquals(5, model.uiState.value.quantity)
        assertFalse(model.uiState.value.canSave)
        assertNull(withTimeoutOrNull(1) { model.event.first() })
    }

    @Test
    fun zeroQuantityNeedsConfirmationAndDismissKeepsDraft() = runTest {
        val model = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to product.id)))
        model.updateProducts(listOf(product))
        model.handleAction(ProductDetailAction.QuantityChanged(0))
        model.handleAction(ProductDetailAction.SaveClicked)
        assertTrue(model.uiState.value.showSaveConfirmation)
        assertNull(withTimeoutOrNull(1) { model.event.first() })
        model.handleAction(ProductDetailAction.SaveConfirmationDismissed)
        assertFalse(model.uiState.value.showSaveConfirmation)
        assertEquals(0, model.uiState.value.quantity)
        assertEquals(product, model.uiState.value.product)
    }

    @Test
    fun confirmedShortageSavesOnceAndCanNavigateToCancellations() = runTest {
        val model = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to product.id)))
        model.updateProducts(listOf(product))
        model.handleAction(ProductDetailAction.QuantityChanged(1))
        model.handleAction(ProductDetailAction.SaveClicked)
        model.handleAction(ProductDetailAction.SaveConfirmClicked)
        assertEquals(ProductDetailEvent.SaveProduct(product.copy(remainingQuantity = 1)), model.event.first())
        assertEquals(2, model.uiState.value.savedShortage)
        assertTrue(model.uiState.value.showSaved)
        assertFalse(model.uiState.value.showSaveConfirmation)
        model.handleAction(ProductDetailAction.SaveConfirmClicked)
        model.handleAction(ProductDetailAction.SaveClicked)
        assertNull(withTimeoutOrNull(1) { model.event.first() })
        model.handleAction(ProductDetailAction.ReservationsCancelClicked)
        assertEquals(ProductDetailEvent.NavigateToCancellations(product.id), model.event.first())
        assertFalse(model.uiState.value.showSaved)
    }

    @Test
    fun availableQuantitySavesWithoutConfirmationAndCompletionNavigatesOnce() = runTest {
        val model = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to product.id)))
        model.updateProducts(listOf(product))
        model.handleAction(ProductDetailAction.QuantityChanged(4))
        model.handleAction(ProductDetailAction.SaveClicked)
        assertEquals(ProductDetailEvent.SaveProduct(product.copy(remainingQuantity = 4)), model.event.first())
        assertFalse(model.uiState.value.showSaveConfirmation)
        assertEquals(0, model.uiState.value.savedShortage)
        model.handleAction(ProductDetailAction.SaveResultConfirmClicked)
        assertEquals(ProductDetailEvent.NavigateBack, model.event.first())
        model.handleAction(ProductDetailAction.SaveResultConfirmClicked)
        assertNull(withTimeoutOrNull(1) { model.event.first() })
    }

    @Test
    fun restoredConfirmationKeepsDraftAndDoesNotSaveUntilConfirmed() = runTest {
        val handle = SavedStateHandle(mapOf("productId" to product.id))
        val model = ProductDetailViewModel(handle)
        model.updateProducts(listOf(product))
        model.handleAction(ProductDetailAction.QuantityChanged(0))
        model.handleAction(ProductDetailAction.SaveClicked)
        val restored = ProductDetailViewModel(SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) }))
        restored.updateProducts(listOf(product))
        assertTrue(restored.uiState.value.showSaveConfirmation)
        assertEquals(0, restored.uiState.value.quantity)
        assertNull(withTimeoutOrNull(1) { restored.event.first() })
        restored.handleAction(ProductDetailAction.SaveConfirmClicked)
        assertEquals(ProductDetailEvent.SaveProduct(product.copy(remainingQuantity = 0)), restored.event.first())
    }

    @Test
    fun editorUpdatesRefreshProductWithoutDiscardingUnchangedQuantityDraft() {
        val model = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to product.id)))
        model.updateProducts(listOf(product))
        model.handleAction(ProductDetailAction.QuantityChanged(4))
        model.updateProducts(listOf(product.copy(name = "Updated peach", salePrice = 3000)))
        assertEquals("Updated peach", model.uiState.value.product?.name)
        assertEquals(4, model.uiState.value.quantity)
        model.updateProducts(listOf(product.copy(remainingQuantity = 8)))
        assertEquals(8, model.uiState.value.quantity)
        assertFalse(model.uiState.value.canSave)
    }

    @Test
    fun missingProductClearsStaleDetailAndAllowsBack() = runTest {
        val model = ProductDetailViewModel(SavedStateHandle(mapOf("productId" to product.id)))
        model.updateProducts(listOf(product))
        model.handleAction(ProductDetailAction.QuantityChanged(0))
        model.handleAction(ProductDetailAction.SaveClicked)
        model.updateProducts(listOf(product.copy(id = "other")))
        assertNull(model.uiState.value.product)
        assertFalse(model.uiState.value.canSave)
        assertFalse(model.uiState.value.showSaveConfirmation)
        model.handleAction(ProductDetailAction.NavigationBackClicked)
        assertEquals(ProductDetailEvent.NavigateBack, model.event.first())
    }
}
