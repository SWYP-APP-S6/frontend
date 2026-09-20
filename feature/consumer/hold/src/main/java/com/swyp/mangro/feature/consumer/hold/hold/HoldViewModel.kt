package com.swyp.mangro.feature.consumer.hold.hold

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.core.designsystem.component.card.timer.TimerCardPhase
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishedProduct
import com.swyp.mangro.core.model.store.StoreInfo
import com.swyp.mangro.data.consumer.hold.model.HoldDetail
import com.swyp.mangro.data.consumer.hold.repository.HoldRepository
import com.swyp.mangro.feature.consumer.hold.navigation.HoldDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HoldViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HoldRepository,
) : ViewModel() {
    private val holdId: Long = savedStateHandle.toRoute<HoldDestination>().holdId.toLong()

    private val _uiState = MutableStateFlow(HoldUiState())
    val uiState: StateFlow<HoldUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HoldUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadHoldInfo()
    }

    fun handleAction(action: HoldUiAction) {
        when (action) {
            is HoldUiAction.OnTimerPhaseChange -> {
                _uiState.update { it.copy(timerPhase = action.phase) }
            }

            is HoldUiAction.OnCancelClick -> {
                viewModelScope.launch {
                    repository.cancelHold(holdId).collect { result ->
                        result
                            .onSuccess {
                                _uiEvent.send(HoldUiEvent.NavigateToProductDetail)
                            }
                            .onFailure {
                                // 실패 처리
                            }
                    }
                }
            }

            is HoldUiAction.OnRetryClick -> {
                viewModelScope.launch {
                    val now = System.currentTimeMillis()
                    _uiState.update {
                        it.copy(
                            requestTimeMillis = now,
                            endTimeMillis = now + 15 * 60_000,
                            timerPhase = TimerCardPhase.DEFAULT,
                        )
                    }
                }
            }

            is HoldUiAction.OnDirectionsClick -> {
                viewModelScope.launch {
                    uiState.value.storeInfo?.let {
                        _uiEvent.send(HoldUiEvent.OpenMapDirections(it))
                    }
                }
            }

            is HoldUiAction.OnCopyAddressClick -> {
                viewModelScope.launch {
                    uiState.value.storeInfo?.let {
                        _uiEvent.send(HoldUiEvent.CopyAddress(it.address))
                    }
                }
            }

            is HoldUiAction.OnCallClick -> {
                viewModelScope.launch {
                    uiState.value.storeInfo?.let {
                        _uiEvent.send(HoldUiEvent.OpenDialer(it.phoneNumber))
                    }
                }
            }

            is HoldUiAction.OnViewOtherProductsClick -> {
                viewModelScope.launch {
                    _uiEvent.send(HoldUiEvent.NavigateToHomeList)
                }
            }
        }
    }

    private fun loadHoldInfo() {
        viewModelScope.launch {
            repository.fetchHold(holdId).collect { result ->
                result
                    .onSuccess { detail ->
                        _uiState.update { detail.toHoldUiState() }
                        if (detail.status == "HOLDING") {
                            startPollingForCompletion()
                        }
                    }
                    .onFailure {
                        // 실패 처리
                    }
            }
        }
    }

    private fun startPollingForCompletion() {
        viewModelScope.launch {
            while (true) {
                delay(5_000)
                var shouldStop = false
                repository.fetchHold(holdId).collect { result ->
                    result
                        .onSuccess { detail ->
                            when (detail.status) {
                                "COMPLETED" -> {
                                    shouldStop = true
                                    _uiEvent.send(HoldUiEvent.NavigateToPickupComplete(holdId.toString()))
                                }
                                "CANCELED", "EXPIRED" -> {
                                    shouldStop = true
                                    _uiState.update { detail.toHoldUiState() }
                                }
                                else -> {
                                    _uiState.update { detail.toHoldUiState() }
                                }
                            }
                        }
                        .onFailure {
                            // 실패 처리
                        }
                }
                if (shouldStop) break
            }
        }
    }
}

private fun HoldDetail.toHoldUiState(): HoldUiState = HoldUiState(
    requestTimeMillis = heldAtMillis,
    endTimeMillis = expiresAtMillis,
    wishedProducts = items.map { item ->
        WishedProduct(
            id = item.productId.toString(),
            imageUrl = item.photoUrl,
            name = item.name,
            quantity = item.qty,
            price = item.salePrice,
            originalPrice = item.originalPrice,
        )
    }.toPersistentList(),
    storeInfo = StoreInfo(
        id = store.id,
        name = store.name,
        address = store.address,
        phoneNumber = store.phone,
        distanceMeters = 0,
        travelInfo = "",
        closingTime = if (store.openNow) {
            "영업중 · ${store.businessOpenTime.take(5)}~${store.businessCloseTime.take(5)}"
        } else {
            "영업종료 · ${store.businessOpenTime.take(5)}~${store.businessCloseTime.take(5)}"
        },
        latitude = store.latitude,
        longitude = store.longitude,
    ),
    isCancelled = status == "CANCELED",
)
