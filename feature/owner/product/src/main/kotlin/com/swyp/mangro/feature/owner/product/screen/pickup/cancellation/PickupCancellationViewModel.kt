package com.swyp.mangro.feature.owner.product.screen.pickup.cancellation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.feature.owner.product.data.OwnerPickupStore
import com.swyp.mangro.feature.owner.product.data.pickupTime
import com.swyp.mangro.feature.owner.product.model.pickupShortages
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
class PickupCancellationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pickupStore: OwnerPickupStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        PickupCancellationState(
            excludedIds = savedStateHandle.get<ArrayList<String>>(EXCLUDED)?.toSet().orEmpty(),
            showConfirmation = savedStateHandle[CONFIRMATION] ?: false,
            confirmationIds = savedStateHandle.get<ArrayList<String>>(CONFIRMATION_IDS)?.toSet().orEmpty(),
        ),
    )
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<PickupCancellationEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(pickupStore.snapshot, pickupTime()) { snapshot, now ->
                snapshot to pickupShortages(snapshot.pickups, snapshot.stock, now)
            }.collect { (snapshot, shortages) ->
                updateState { state ->
                    val next = state.copy(shortages = shortages, storeName = snapshot.storeName, storePhone = snapshot.storePhone)
                    next.copy(showConfirmation = next.showConfirmation && next.confirmationIds.isNotEmpty() && next.targets.map { it.id }.containsAll(next.confirmationIds))
                }
            }
        }
    }

    fun handleAction(action: PickupCancellationAction) {
        when (action) {
            is PickupCancellationAction.SelectionChanged -> {
                if (uiState.value.showConfirmation || uiState.value.targets.none { it.id == action.id }) return
                updateState { it.copy(excludedIds = if (action.selected) it.excludedIds - action.id else it.excludedIds + action.id) }
            }
            PickupCancellationAction.CancelClicked -> updateState { it.copy(showConfirmation = it.selectedIds.isNotEmpty(), confirmationIds = it.selectedIds, hasError = false) }
            PickupCancellationAction.ConfirmationDismissed -> updateState { it.copy(showConfirmation = false, hasError = false) }
            PickupCancellationAction.ConfirmationClicked -> {
                val state = uiState.value
                if (!state.showConfirmation) return
                val success = pickupStore.cancel(state.selectedIds)
                updateState { it.copy(showConfirmation = !success, hasError = !success) }
            }
            PickupCancellationAction.NavigationBackClicked -> {
                if (uiState.value.showConfirmation) {
                    updateState { it.copy(showConfirmation = false) }
                } else {
                    _event.trySend(PickupCancellationEvent.NavigateBack)
                }
            }
        }
    }

    private fun updateState(transform: (PickupCancellationState) -> PickupCancellationState) {
        _uiState.update(transform)
        savedStateHandle[EXCLUDED] = ArrayList(uiState.value.excludedIds)
        savedStateHandle[CONFIRMATION] = uiState.value.showConfirmation
        savedStateHandle[CONFIRMATION_IDS] = ArrayList(uiState.value.confirmationIds)
    }

    private companion object {
        const val EXCLUDED = "pickupCancellationExcluded"
        const val CONFIRMATION_IDS = "pickupCancellationConfirmedIds"
        const val CONFIRMATION = "pickupCancellationConfirmation"
    }
}
