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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: OwnerProductRepository,
) : ViewModel() {
    private val productId = savedStateHandle.toRoute<OwnerProductDetailDestination>().productId

    private val _uiState = MutableStateFlow(ProductDetailState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<ProductDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        loadProduct()
    }

    private fun retry() {
        if (uiState.value.isLoading || uiState.value.isSaving || uiState.value.showSaved || uiState.value.showSaveConfirmation) return
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            repository
                .fetchProduct((productId.toLongOrNull() ?: 0L))
                .onStart {
                    updateState { it.copy(isLoading = true, hasError = false) }
                }.collect { result ->
                    result
                        .onSuccess { value ->
                            val product = value.presentation()

                            updateState {
                                it.copy(
                                    product = product,
                                    quantity = product.remainingQuantity,
                                    isLoading = false,
                                )
                            }
                        }.onFailure {
                            updateState { it.copy(isLoading = false, hasError = true) }
                        }
                }
        }
    }

    fun handleAction(action: ProductDetailAction) {
        when (action) {
            ProductDetailAction.RetryClicked -> {
                if (uiState.value.hasError) retry()
            }

            is ProductDetailAction.QuantityChanged -> {
                if (action.quantity in (uiState.value.product?.minAdjustableQuantity ?: 0)..9999 && !uiState.value.isSaving && !uiState.value.showSaveConfirmation && !uiState.value.showSaved) {
                    updateState { it.copy(quantity = action.quantity) }
                }
            }

            ProductDetailAction.SaveClicked -> {
                val product = uiState.value.product ?: return
                if (!uiState.value.canSave || uiState.value.showSaveConfirmation) return

                if (uiState.value.quantity == 0 || uiState.value.quantity < product.reservedQuantity) {
                    updateState { it.copy(showSaveConfirmation = true) }
                } else {
                    save()
                }
            }

            ProductDetailAction.SaveConfirmClicked -> {
                if (uiState.value.showSaveConfirmation) save()
            }

            ProductDetailAction.SaveConfirmationDismissed -> {
                updateState { it.copy(showSaveConfirmation = false) }
            }

            ProductDetailAction.SaveResultDismissed -> {
                updateState { it.copy(showSaved = false) }
            }

            ProductDetailAction.SaveResultConfirmClicked -> {
                if (!uiState.value.showSaved || uiState.value.savedShortage > 0) return

                updateState { it.copy(showSaved = false) }

                viewModelScope.launch {
                    _event.send(ProductDetailEvent.NavigateBack)
                }
            }

            ProductDetailAction.NavigationBackClicked -> {
                viewModelScope.launch {
                    _event.send(ProductDetailEvent.NavigateBack)
                }
            }

            ProductDetailAction.ReservationsCancelClicked -> {
                updateState { it.copy(showSaved = false) }

                viewModelScope.launch {
                    _event.send(ProductDetailEvent.NavigateToCancellations(productId))
                }
            }
        }
    }

    private fun save() {
        if (!uiState.value.canSave) return

        updateState { it.copy(isSaving = true, hasError = false) }
        viewModelScope.launch {
            repository
                .updateStock((productId.toLongOrNull() ?: 0L), uiState.value.quantity)
                .collect { result ->
                    result
                        .onSuccess { value ->
                            val updated = value.presentation()
                            updateState {
                                it.copy(
                                    product = updated,
                                    quantity = updated.remainingQuantity,
                                    isSaving = false,
                                    showSaveConfirmation = false,
                                    showSaved = true,
                                    savedShortage = value.shortfallQuantity,
                                )
                            }
                        }.onFailure {
                            updateState { it.copy(isSaving = false, showSaveConfirmation = false, hasError = true) }
                        }
                }
        }
    }

    private fun updateState(transform: (ProductDetailState) -> ProductDetailState) {
        _uiState.value = transform(uiState.value)
    }
}
