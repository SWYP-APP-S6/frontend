package com.swyp.mangro.feature.owner.product.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.swyp.mangro.feature.owner.product.data.OwnerPickupStore
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pickupStore: OwnerPickupStore,
) : ViewModel() {
    private val productId = savedStateHandle.toRoute<OwnerProductDetailDestination>().productId

    val uiState = savedStateHandle.getStateFlow(STATE, ProductDetailState())

    private val _event = Channel<ProductDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun updateProducts(products: List<OwnerProductModel>) {
        val product = products.find { it.id == productId }
        if (product == null) {
            updateState { ProductDetailState() }
            return
        }
        val current = uiState.value
        val previous = current.product
        updateState {
            if (previous == null || previous.id != product.id) {
                ProductDetailState(product = product, quantity = product.remainingQuantity)
            } else {
                current.copy(
                    product = product,
                    quantity = if (previous.remainingQuantity != product.remainingQuantity) product.remainingQuantity else current.quantity,
                )
            }
        }
    }

    fun handleAction(action: ProductDetailAction) {
        when (action) {
            is ProductDetailAction.QuantityChanged -> {
                if (action.quantity >= 0 && !uiState.value.showSaveConfirmation && !uiState.value.showSaved) {
                    updateState { it.copy(quantity = action.quantity) }
                }
            }

            ProductDetailAction.SaveClicked -> {
                val state = uiState.value
                val product = state.product ?: return
                if (!state.canSave || state.showSaveConfirmation) return
                if (state.quantity == 0 || state.quantity < product.reservedQuantity) {
                    updateState { it.copy(showSaveConfirmation = true) }
                } else {
                    save()
                }
            }

            ProductDetailAction.SaveConfirmClicked -> if (uiState.value.showSaveConfirmation) save()

            ProductDetailAction.SaveConfirmationDismissed -> updateState { it.copy(showSaveConfirmation = false) }

            ProductDetailAction.SaveResultDismissed -> updateState { it.copy(showSaved = false) }

            ProductDetailAction.SaveResultConfirmClicked -> {
                if (!uiState.value.showSaved || uiState.value.savedShortage > 0) return
                updateState { it.copy(showSaved = false) }
                _event.trySend(ProductDetailEvent.NavigateBack)
            }

            ProductDetailAction.NavigationBackClicked -> _event.trySend(ProductDetailEvent.NavigateBack)

            ProductDetailAction.ReservationsCancelClicked -> {
                updateState { it.copy(showSaved = false) }
                _event.trySend(ProductDetailEvent.NavigateToCancellations(productId))
            }
        }
    }

    private fun save() {
        val state = uiState.value
        val product = state.product ?: return
        if (!state.canSave) return
        val updated = product.copy(remainingQuantity = state.quantity)
        pickupStore.updateProducts(listOf(updated))
        updateState {
            it.copy(product = updated, showSaveConfirmation = false, showSaved = true, savedShortage = updated.shortageQuantity)
        }
        _event.trySend(ProductDetailEvent.SaveProduct(updated))
    }

    private fun updateState(transform: (ProductDetailState) -> ProductDetailState) {
        savedStateHandle[STATE] = transform(uiState.value)
    }

    private companion object {
        const val STATE = "productDetailState"
    }
}
