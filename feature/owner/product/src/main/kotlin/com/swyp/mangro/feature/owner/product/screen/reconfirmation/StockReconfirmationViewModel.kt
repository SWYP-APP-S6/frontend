package com.swyp.mangro.feature.owner.product.screen.reconfirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.owner.home.repository.OwnerHomeRepository
import com.swyp.mangro.data.owner.product.repository.StockReconfirmation
import com.swyp.mangro.data.owner.product.repository.StockReconfirmationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StockReconfirmationState(val product: StockReconfirmation? = null, val isLoading: Boolean = false, val isSaving: Boolean = false, val hasError: Boolean = false)

@HiltViewModel
class StockReconfirmationViewModel @Inject constructor(
    private val homeRepository: OwnerHomeRepository,
    private val repository: StockReconfirmationRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(StockReconfirmationState())
    val state = _state.asStateFlow()
    private val _editProduct = Channel<Long>(Channel.BUFFERED)
    val editProduct = _editProduct.receiveAsFlow()
    private val pending = ArrayDeque<Long>()
    private var lastKey: String? = null
    private var queuedKey: String? = null

    fun open(key: String) {
        if (key == lastKey) return
        if (state.value.isLoading || state.value.isSaving || state.value.product != null) {
            queuedKey = key
            return
        }
        lastKey = key
        reload()
    }

    fun reload() {
        if (state.value.isLoading || state.value.isSaving) return
        _state.value = StockReconfirmationState(isLoading = true)
        viewModelScope.launch {
            homeRepository.fetchHome().first().fold(
                onSuccess = { home ->
                    pending.clear()
                    pending.addAll(home.products.filter { it.reconfirmPending }.map { it.id }.distinct())
                    showNext()
                },
                onFailure = { _state.value = StockReconfirmationState(hasError = true) },
            )
        }
    }

    private suspend fun showNext() {
        while (pending.isNotEmpty()) {
            val result = repository.fetch(pending.removeFirst())
            if (result.isFailure) {
                _state.value = StockReconfirmationState(hasError = true)
                return
            }
            val product = result.getOrNull() ?: continue
            _state.value = StockReconfirmationState(product = product)
            return
        }
        _state.value = StockReconfirmationState()
        queuedKey?.let { key ->
            queuedKey = null
            open(key)
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
                        showNext()
                    } else {
                        pending.clear()
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
        pending.clear()
        queuedKey = null
        _state.value = StockReconfirmationState()
    }
}
