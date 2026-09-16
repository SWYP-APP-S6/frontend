package com.swyp.mangro.feature.owner.product.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import com.swyp.mangro.feature.owner.product.model.presentation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: OwnerProductRepository,
) : ViewModel() {
    private val productId = savedStateHandle.toRoute<OwnerProductDetailDestination>().productId

    private val _uiState = MutableStateFlow(ProductDetailState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<ProductDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun refresh() {
        if (uiState.value.isLoading || uiState.value.isSaving || uiState.value.showSaved || uiState.value.showSaveConfirmation) return
        updateState { it.copy(isLoading = true, hasError = false) }
        viewModelScope.launch {
            repository.fetchProduct((productId.toLongOrNull() ?: 0L)).first().fold(
                onSuccess = { value ->
                    val product = value.presentation()
                    updateState { it.copy(product = product, quantity = product.remainingQuantity, isLoading = false) }
                },
                onFailure = { updateState { it.copy(isLoading = false, hasError = true) } },
            )
        }
    }

    fun handleAction(action: ProductDetailAction) {
        when (action) {
            ProductDetailAction.Refresh -> refresh()
            is ProductDetailAction.QuantityChanged -> {
                if (action.quantity in (uiState.value.product?.minAdjustableQuantity ?: 0)..9999 && !uiState.value.isSaving && !uiState.value.showSaveConfirmation && !uiState.value.showSaved) {
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
        if (!state.canSave) return
        updateState { it.copy(isSaving = true, hasError = false) }
        viewModelScope.launch {
            repository.updateStock((productId.toLongOrNull() ?: 0L), state.quantity).first().fold(
                onSuccess = { value ->
                    val updated = value.presentation()
                    updateState { it.copy(product = updated, quantity = updated.remainingQuantity, isSaving = false, showSaveConfirmation = false, showSaved = true, savedShortage = value.shortfallQuantity) }
                },
                onFailure = { updateState { it.copy(isSaving = false, showSaveConfirmation = false, hasError = true) } },
            )
        }
    }

    private fun updateState(transform: (ProductDetailState) -> ProductDetailState) {
        _uiState.value = transform(uiState.value)
    }
}
