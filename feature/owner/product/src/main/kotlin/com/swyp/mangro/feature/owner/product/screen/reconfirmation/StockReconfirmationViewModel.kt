package com.swyp.mangro.feature.owner.product.screen.reconfirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.owner.product.repository.StockReconfirmation
import com.swyp.mangro.data.owner.product.repository.StockReconfirmationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StockReconfirmationState(val product: StockReconfirmation? = null, val isLoading: Boolean = false, val isSaving: Boolean = false, val hasError: Boolean = false)

@HiltViewModel
class StockReconfirmationViewModel @Inject constructor(
    private val repository: StockReconfirmationRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(StockReconfirmationState())
    val state = _state.asStateFlow()
    private val _editProduct = Channel<Long>(Channel.BUFFERED)
    val editProduct = _editProduct.receiveAsFlow()
    private var lastKey: String? = null
    private var activeProductId: Long? = null
    private var queuedRequest: ReconfirmationRequest? = null

    fun open(key: String, productId: Long) {
        if (productId <= 0 || key == lastKey) return
        val request = ReconfirmationRequest(key, productId)
        if (state.value.isLoading || state.value.isSaving || state.value.product != null) {
            queuedRequest = request
            return
        }
        activate(request)
    }

    fun reload() {
        if (state.value.isLoading || state.value.isSaving) return
        val productId = activeProductId ?: return
        _state.value = StockReconfirmationState(isLoading = true)
        viewModelScope.launch {
            repository.fetch(productId).fold(
                onSuccess = { product ->
                    if (product == null) {
                        finishCurrent()
                    } else {
                        _state.value = StockReconfirmationState(product = product)
                    }
                },
                onFailure = { _state.value = StockReconfirmationState(hasError = true) },
            )
        }
    }

    private fun activate(request: ReconfirmationRequest) {
        lastKey = request.key
        activeProductId = request.productId
        reload()
    }

    private fun finishCurrent() {
        activeProductId = null
        _state.value = StockReconfirmationState()
        queuedRequest?.let { request ->
            queuedRequest = null
            activate(request)
        }
    }

    fun answer(confirmed: Boolean) {
        val product = state.value.product ?: return
        if (state.value.isSaving || state.value.isLoading) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.answer(product.productId, confirmed).fold(
                onSuccess = {
                    if (confirmed) {
                        finishCurrent()
                    } else {
                        activeProductId = null
                        queuedRequest = null
                        _state.value = StockReconfirmationState()
                        _editProduct.send(product.productId)
                    }
                },
                // Do not resend a mutation automatically after an uncertain response.
                onFailure = { _state.value = StockReconfirmationState(hasError = true) },
            )
        }
    }

    fun dismiss() {
        if (state.value.isLoading || state.value.isSaving) return
        activeProductId = null
        queuedRequest = null
        _state.value = StockReconfirmationState()
    }
}

private data class ReconfirmationRequest(val key: String, val productId: Long)
