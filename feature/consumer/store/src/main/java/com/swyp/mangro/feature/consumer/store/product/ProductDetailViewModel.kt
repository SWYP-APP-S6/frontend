package com.swyp.mangro.feature.consumer.store.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<ProductDetailUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadProductDetail()
    }

    fun handleAction(action: ProductDetailUiAction) {
        when (action) {
            is ProductDetailUiAction.OnWishButtonClick -> {
                _uiState.update { it.copy(isWishBottomSheetVisible = true) }
            }

            is ProductDetailUiAction.OnQuantityChange -> {
                _uiState.update { it.copy(wishState = it.wishState.copy(quantity = action.quantity)) }
            }

            is ProductDetailUiAction.OnWishConfirmClick -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isWishBottomSheetVisible = false) }
                    _uiEvent.send(ProductDetailUiEvent.WishConfirmed)
                }
            }

            is ProductDetailUiAction.OnWishBottomSheetDismiss -> {
                _uiState.update { it.copy(isWishBottomSheetVisible = false) }
            }

            is ProductDetailUiAction.OnStoreInfoClick -> {
                viewModelScope.launch {
                    _uiEvent.send(ProductDetailUiEvent.NavigateToStoreDetail)
                }
            }
        }
    }

    private fun loadProductDetail() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(productInfo = dummyProductInfo)
            }
        }
    }
}
