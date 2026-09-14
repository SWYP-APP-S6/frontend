package com.swyp.mangro.feature.owner.product.screen.editor

import androidx.lifecycle.SavedStateHandle
import com.swyp.mangro.feature.owner.product.model.ProductDraftModel
import com.swyp.mangro.feature.owner.product.screen.editor.basic.ProductBasicInfoAction
import com.swyp.mangro.feature.owner.product.screen.editor.basic.ProductBasicInfoEvent
import com.swyp.mangro.feature.owner.product.screen.editor.basic.ProductBasicInfoViewModel
import com.swyp.mangro.feature.owner.product.screen.editor.pickup.ProductPickupInfoAction
import com.swyp.mangro.feature.owner.product.screen.editor.pickup.ProductPickupInfoEvent
import com.swyp.mangro.feature.owner.product.screen.editor.pickup.ProductPickupInfoViewModel
import com.swyp.mangro.feature.owner.product.screen.editor.price.ProductPriceAction
import com.swyp.mangro.feature.owner.product.screen.editor.price.ProductPriceEvent
import com.swyp.mangro.feature.owner.product.screen.editor.price.ProductPriceViewModel
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductEditorViewModelTest {
    private val clock = Clock.fixed(Instant.parse("2026-09-14T10:00:00Z"), ZoneOffset.UTC)

    @Test
    fun invalidOrExpiredPickupTimeCannotBeSelectedOrSaved() = runTest {
        val handle = SavedStateHandle()
        val input = ProductDraftModel(id = "peach", name = "복숭아", photos = listOf("file://peach"), originalPrice = 10000, salePrice = 4000)
        val vm = ProductPickupInfoViewModel(handle, clock)
        vm.initialize(input, "20:00", "16:00")
        assertEquals("16:00", vm.uiState.value.pickupTimeOptions.first())
        vm.handleAction(ProductPickupInfoAction.PickupTimeChanged("15:00"))
        assertNull(vm.uiState.value.pickupTime)
        vm.handleAction(ProductPickupInfoAction.PickupTimeChanged("19:00"))
        vm.handleAction(ProductPickupInfoAction.RegisterClicked)
        assertTrue(vm.uiState.value.showPreview)
        val late = ProductPickupInfoViewModel(handle, Clock.offset(clock, java.time.Duration.ofHours(11)))
        late.initialize(input, "20:00", "16:00")
        assertTrue(late.uiState.value.pickupTimeOptions.isEmpty())
        assertFalse(late.uiState.value.canPreview)
        assertFalse(late.uiState.value.showPreview)
        late.handleAction(ProductPickupInfoAction.SaveClicked)
        assertNull(withTimeoutOrNull(1) { late.event.first() })
    }

    @Test
    fun basicDraftSurvivesViewModelRecreationWithoutBeingOverwritten() {
        val handle = SavedStateHandle()
        val original = ProductBasicInfoViewModel(handle)
        original.initialize()
        original.handleAction(ProductBasicInfoAction.NameChanged("복숭아"))
        original.handleAction(ProductBasicInfoAction.PhotosSelected(listOf("file://peach")))
        val restored = ProductBasicInfoViewModel(handle)
        restored.initialize()
        assertEquals("복숭아", restored.uiState.value.name)
        assertTrue(restored.uiState.value.canContinue)
        restored.handleAction(ProductBasicInfoAction.PhotoRemoveClicked("file://peach"))
        assertFalse(restored.uiState.value.canContinue)
    }

    @Test
    fun invalidPricesCannotContinueAndNewQuantityCannotBecomeZero() {
        val vm = ProductPriceViewModel(SavedStateHandle())
        vm.initialize(ProductDraftModel(id = "peach"))
        vm.handleAction(ProductPriceAction.OriginalPriceChanged("1000"))
        vm.handleAction(ProductPriceAction.SalePriceChanged("2000"))
        assertFalse(vm.uiState.value.canContinue)
        vm.handleAction(ProductPriceAction.SalePriceChanged("400"))
        vm.handleAction(ProductPriceAction.QuantityChanged(0))
        assertEquals(1, vm.uiState.value.quantity)
        assertEquals(60, vm.uiState.value.discount)
        assertTrue(vm.uiState.value.canContinue)
    }

    @Test
    fun pickupPreviewIncludesPendingTagAndSaveIsEmittedOnce() = runTest {
        val vm = ProductPickupInfoViewModel(SavedStateHandle(), clock)
        vm.initialize(ProductDraftModel(id = "peach", name = "복숭아", photos = listOf("file://peach"), originalPrice = 10000, salePrice = 4000, quantity = 3), "20:00", "09:00")
        vm.handleAction(ProductPickupInfoAction.TagChanged("청과"))
        vm.handleAction(ProductPickupInfoAction.RegisterClicked)
        assertTrue(vm.uiState.value.showPreview)
        assertEquals(listOf("청과"), vm.uiState.value.draft!!.tags)
        assertEquals("", vm.uiState.value.tagInput)
        vm.handleAction(ProductPickupInfoAction.SaveClicked)
        val saved = (vm.event.first() as ProductPickupInfoEvent.Save).product
        assertEquals(3, saved.initialQuantity)
        assertEquals("20:00", saved.pickupEndTime)
        assertFalse(vm.uiState.value.showPreview)
        vm.handleAction(ProductPickupInfoAction.SaveClicked)
        assertNull(withTimeoutOrNull(1) { vm.event.first() })
    }

    @Test
    fun changedBasicInfoDoesNotOverwriteRestoredPriceInput() = runTest {
        val handle = SavedStateHandle()
        val draft = ProductDraftModel(id = "peach", name = "처음 이름")
        val vm = ProductPriceViewModel(handle)
        vm.initialize(draft)
        vm.handleAction(ProductPriceAction.OriginalPriceChanged("10000"))
        vm.handleAction(ProductPriceAction.SalePriceChanged("잘못된 입력"))
        val restored = ProductPriceViewModel(handle)
        restored.initialize(draft.copy(name = "변경된 이름"))
        assertEquals("잘못된 입력", restored.uiState.value.salePrice)
        assertFalse(restored.uiState.value.canContinue)
        restored.handleAction(ProductPriceAction.SalePriceChanged("3000"))
        restored.handleAction(ProductPriceAction.NextClicked)
        val next = restored.event.first() as com.swyp.mangro.feature.owner.product.screen.editor.price.ProductPriceEvent.Next
        assertEquals("변경된 이름", next.draft.name)
        assertEquals(3000, next.draft.salePrice)
    }

    @Test
    fun changedProductPreservesPickupInputsAfterRecreation() {
        val handle = SavedStateHandle()
        val draft = ProductDraftModel(id = "peach", name = "복숭아", originalPrice = 10000, salePrice = 4000)
        val vm = ProductPickupInfoViewModel(handle, clock)
        vm.initialize(draft, "20:00", "09:00")
        vm.handleAction(ProductPickupInfoAction.TagChanged("작성 중"))
        vm.handleAction(ProductPickupInfoAction.PickupTimeChanged("19:00"))
        val restored = ProductPickupInfoViewModel(handle, clock)
        restored.initialize(draft.copy(name = "수정된 복숭아", salePrice = 3000), "20:00", "09:00")
        assertEquals("작성 중", restored.uiState.value.tagInput)
        assertEquals("19:00", restored.uiState.value.pickupTime)
        assertEquals("수정된 복숭아", restored.uiState.value.product?.name)
        assertEquals(3000, restored.uiState.value.product?.salePrice)
    }

    @Test
    fun newProductFlowsThroughAllStepsWithStableIdentity() = runTest {
        val handle = SavedStateHandle()
        val basic = ProductBasicInfoViewModel(handle)
        basic.initialize()
        basic.handleAction(ProductBasicInfoAction.NameChanged("새 상품"))
        basic.handleAction(ProductBasicInfoAction.PhotosSelected(listOf("file://fixture")))
        basic.handleAction(ProductBasicInfoAction.NextClicked)
        val first = (basic.event.first() as ProductBasicInfoEvent.Next).draft
        val restored = ProductBasicInfoViewModel(handle)
        restored.initialize()
        restored.handleAction(ProductBasicInfoAction.NextClicked)
        assertEquals(first.id, (restored.event.first() as ProductBasicInfoEvent.Next).draft.id)
        val price = ProductPriceViewModel(SavedStateHandle())
        price.initialize(first)
        price.handleAction(ProductPriceAction.OriginalPriceChanged("10000"))
        price.handleAction(ProductPriceAction.SalePriceChanged("4000"))
        price.handleAction(ProductPriceAction.QuantityChanged(3))
        price.handleAction(ProductPriceAction.NextClicked)
        val pickup = ProductPickupInfoViewModel(SavedStateHandle(), clock)
        pickup.initialize((price.event.first() as ProductPriceEvent.Next).draft, "20:00", "09:00")
        pickup.handleAction(ProductPickupInfoAction.PickupTimeChanged("19:00"))
        pickup.handleAction(ProductPickupInfoAction.TagChanged("과일"))
        pickup.handleAction(ProductPickupInfoAction.RegisterClicked)
        pickup.handleAction(ProductPickupInfoAction.SaveClicked)
        val saved = (pickup.event.first() as ProductPickupInfoEvent.Save).product
        assertEquals(first.id, saved.id)
        assertEquals("새 상품", saved.name)
        assertEquals(listOf("file://fixture"), saved.photos)
        assertEquals(4000, saved.salePrice)
        assertEquals(3, saved.initialQuantity)
        assertEquals(3, saved.remainingQuantity)
        assertEquals(0, saved.reservedQuantity)
        assertEquals("19:00", saved.pickupEndTime)
        assertEquals(listOf("과일"), saved.tags)
    }
}
