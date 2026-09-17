package com.swyp.mangro.feature.owner.product.screen.pickup.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.data.owner.product.model.HoldDetail
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import com.swyp.mangro.feature.owner.product.model.Pickup
import com.swyp.mangro.feature.owner.product.model.presentation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PickupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ownerProductRepository: OwnerProductRepository,
) : ViewModel() {
    private val pickupId = savedStateHandle.toRoute<OwnerPickupDetailDestination>().pickupId.toLongOrNull() ?: 0L
    private val _uiState = MutableStateFlow(PickupDetailState())
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<PickupDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()
    private var offset = 0L

    init {
        loadPickup()

        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(now = System.currentTimeMillis() + offset) }
                delay(1000.milliseconds)
            }
        }
    }

    private fun refresh() {
        if (uiState.value.isLoading || uiState.value.isSaving) return
        loadPickup()
    }

    private fun loadPickup() {
        viewModelScope.launch {
            ownerProductRepository
                .fetchHold(pickupId)
                .onStart {
                    _uiState.update { it.copy(isLoading = true, hasError = false, canComplete = false) }
                }
                .collect { result ->
                    result.onSuccess { detail ->
                        updateDetail(detail)
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false, hasError = true, canComplete = false) }
                    }
                }
        }
    }

    private fun updateDetail(detail: HoldDetail) {
        offset = detail.serverTime - System.currentTimeMillis()

        val first = detail.items.firstOrNull()

        _uiState.update {
            it.copy(
                pickup = first?.let { item ->
                    Pickup(
                        id = pickupId.toString(),
                        productId = item.productId.toString(),
                        productName = item.name,
                        customerName = detail.nickname,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice.toLong(),
                        requestedAt = detail.heldAt,
                        deadline = detail.expiresAt,
                        status = detail.status.presentation(),
                        completedAt = detail.completedAt,
                    )
                },
                items = detail.items,
                totalPrice = detail.totalPrice.toLong(),
                storeName = detail.storeName,
                now = detail.serverTime,
                isLoading = false,
                isSaving = false,
                hasError = first == null,
                canComplete = detail.status == HoldStatus.HOLDING && first != null,
            )
        }
    }

    fun handleAction(action: PickupDetailAction) {
        when (action) {
            PickupDetailAction.Refresh -> refresh()

            PickupDetailAction.CompleteClicked -> {
                val state = uiState.value
                if (!state.canComplete || state.isSaving || state.isLoading || System.currentTimeMillis() + offset >= (state.pickup?.deadline ?: 0)) return
                viewModelScope.launch {
                    ownerProductRepository
                        .markAsPickedUp(pickupId)
                        .onStart {
                            _uiState.update { it.copy(isSaving = true, canComplete = false, hasError = false) }
                        }
                        .collect { result ->
                            result.onSuccess { detail ->
                                updateDetail(detail)
                                _event.trySend(PickupDetailEvent.HoldsChanged)
                            }.onFailure {
                                _uiState.update { it.copy(isSaving = false, hasError = true, canComplete = false) }
                            }
                        }
                }
            }

            PickupDetailAction.HomeClicked -> _event.trySend(PickupDetailEvent.NavigateToHome)

            PickupDetailAction.NavigationBackClicked -> _event.trySend(PickupDetailEvent.NavigateBack)
        }
    }
}
