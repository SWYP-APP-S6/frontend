package com.swyp.mangro.feature.owner.product.screen.pickup.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.feature.owner.product.data.OwnerPickupStore
import com.swyp.mangro.feature.owner.product.data.pickupTime
import com.swyp.mangro.feature.owner.product.model.canCompletePickup
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PickupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pickupStore: OwnerPickupStore,
) : ViewModel() {
    private val pickupId = savedStateHandle.toRoute<OwnerPickupDetailDestination>().pickupId
    private val _uiState = MutableStateFlow(PickupDetailState())
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<PickupDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(pickupStore.snapshot, pickupTime()) { snapshot, now ->
                val pickup = snapshot.pickups.find { it.id == pickupId }
                PickupDetailState(
                    pickup = pickup,
                    storeName = snapshot.storeName,
                    now = now,
                    canComplete = snapshot.isDemo && pickup != null && canCompletePickup(pickup, snapshot.pickups, snapshot.stock, now),
                )
            }.collect { state -> _uiState.update { state.copy(hasError = it.hasError) } }
        }
    }

    fun handleAction(action: PickupDetailAction) {
        when (action) {
            PickupDetailAction.CompleteClicked -> {
                val completed = pickupStore.complete(pickupId)
                _uiState.update { it.copy(hasError = !completed) }
            }
            PickupDetailAction.HomeClicked -> _event.trySend(PickupDetailEvent.NavigateToHome)
            PickupDetailAction.NavigationBackClicked -> _event.trySend(PickupDetailEvent.NavigateBack)
        }
    }
}
