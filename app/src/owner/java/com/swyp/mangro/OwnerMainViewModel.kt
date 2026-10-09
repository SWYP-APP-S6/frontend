package com.swyp.mangro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.owner.product.repository.StockReconfirmation
import com.swyp.mangro.data.owner.product.repository.StockReconfirmationRepository
import com.swyp.mangro.notification.model.OwnerNotificationEvent
import com.swyp.mangro.notification.provider.OwnerNotificationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OwnerMainViewModel @Inject constructor(
    private val ownerNotificationProvider: OwnerNotificationProvider,
    private val stockRepository: StockReconfirmationRepository,
) : ViewModel() {
    private val _stockReconfirmationState = MutableStateFlow(StockReconfirmationState())
    val stockReconfirmationState = _stockReconfirmationState.asStateFlow()

    private val _editProduct = Channel<Long>(Channel.BUFFERED)
    val editProduct = _editProduct.receiveAsFlow()

    private var isMainResumed = false
    private var lastStockRequestKey: String? = null
    private var activeStockProductId: Long? = null
    private var pendingStockRequest: OwnerNotificationEvent.StockReconfirmationRequested? = null
    private var stockJob: Job? = null

    val homeRefreshRequests = ownerNotificationProvider.observeEvents()
        .filterIsInstance<OwnerNotificationEvent.RefreshHome>()
        .conflate()

    init {
        viewModelScope.launch {
            ownerNotificationProvider.observeEvents()
                .filterIsInstance<OwnerNotificationEvent.StockReconfirmationRequested>()
                .collect { openStockReconfirmation(it.key, it.productId) }
        }
    }

    fun onMainResumed() {
        isMainResumed = true
        processPendingStockRequest()
    }

    fun onMainPaused() {
        isMainResumed = false
    }

    fun openStockReconfirmation(key: String, productId: Long) {
        if (productId <= 0) return
        val request = OwnerNotificationEvent.StockReconfirmationRequested(key, productId)
        if (key == lastStockRequestKey) {
            ownerNotificationProvider.consumeStockReconfirmation(request)
            return
        }

        val state = stockReconfirmationState.value
        if (!isMainResumed || state.isLoading || state.isSaving || state.product != null) {
            pendingStockRequest = request
            return
        }

        lastStockRequestKey = key
        activeStockProductId = productId
        reloadStockReconfirmation()
        ownerNotificationProvider.consumeStockReconfirmation(request)
    }

    fun reloadStockReconfirmation() {
        val state = stockReconfirmationState.value
        if (state.isLoading || state.isSaving) return
        val productId = activeStockProductId ?: return
        _stockReconfirmationState.value = StockReconfirmationState(isLoading = true)
        stockJob = viewModelScope.launch {
            stockRepository.fetch(productId).fold(
                onSuccess = { product ->
                    if (product == null) {
                        finishStockReconfirmation()
                    } else {
                        _stockReconfirmationState.value = StockReconfirmationState(product = product)
                    }
                },
                onFailure = { _stockReconfirmationState.value = StockReconfirmationState(hasError = true) },
            )
        }
    }

    fun answerStockReconfirmation(confirmed: Boolean) {
        val state = stockReconfirmationState.value
        val product = state.product ?: return
        if (state.isSaving || state.isLoading) return
        _stockReconfirmationState.update { it.copy(isSaving = true) }
        stockJob = viewModelScope.launch {
            stockRepository.answer(product.productId, confirmed).fold(
                onSuccess = {
                    if (confirmed) {
                        finishStockReconfirmation()
                    } else {
                        dismissPendingStockRequest()
                        activeStockProductId = null
                        _stockReconfirmationState.value = StockReconfirmationState()
                        _editProduct.send(product.productId)
                    }
                },
                onFailure = { _stockReconfirmationState.value = StockReconfirmationState(hasError = true) },
            )
        }
    }

    fun dismissStockReconfirmation() {
        val state = stockReconfirmationState.value
        if (state.isLoading || state.isSaving) return
        dismissPendingStockRequest()
        activeStockProductId = null
        _stockReconfirmationState.value = StockReconfirmationState()
    }

    private fun finishStockReconfirmation() {
        activeStockProductId = null
        _stockReconfirmationState.value = StockReconfirmationState()
        processPendingStockRequest()
    }

    private fun processPendingStockRequest() {
        val state = stockReconfirmationState.value
        if (!isMainResumed || state.isLoading || state.isSaving || state.product != null) return
        val request = pendingStockRequest ?: return
        pendingStockRequest = null
        openStockReconfirmation(request.key, request.productId)
    }

    private fun dismissPendingStockRequest() {
        pendingStockRequest?.let(ownerNotificationProvider::consumeStockReconfirmation)
        pendingStockRequest = null
    }

    fun clearNotificationRequests() {
        ownerNotificationProvider.clearPendingRequests()
        isMainResumed = false
        stockJob?.cancel()
        pendingStockRequest = null
        activeStockProductId = null
        lastStockRequestKey = null
        _stockReconfirmationState.value = StockReconfirmationState()
        while (_editProduct.tryReceive().isSuccess) Unit
    }

    override fun onCleared() {
        _editProduct.cancel()
    }
}

data class StockReconfirmationState(
    val product: StockReconfirmation? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasError: Boolean = false,
)
