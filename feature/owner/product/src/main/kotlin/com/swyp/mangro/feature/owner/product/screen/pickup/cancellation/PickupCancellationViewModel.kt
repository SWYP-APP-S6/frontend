package com.swyp.mangro.feature.owner.product.screen.pickup.cancellation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.owner.product.model.HoldCancellations
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import com.swyp.mangro.feature.owner.product.model.CancellationGroup
import com.swyp.mangro.feature.owner.product.model.CancellationTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PickupCancellationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: OwnerProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PickupCancellationState())
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<PickupCancellationEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun refresh() {
        if (uiState.value.isLoading || uiState.value.isSaving) return
        _uiState.update { it.copy(isLoading = true, showConfirmation = false, hasError = false) }
        viewModelScope.launch {
            repository.fetchCancellations().first().fold(
                onSuccess = ::show,
                onFailure = { _uiState.update { it.copy(isLoading = false, hasError = true) } },
            )
        }
    }

    private fun show(result: HoldCancellations) {
        val groups = result.products.map { product ->
            CancellationGroup(
                product.id.toString(),
                product.name,
                product.candidates.sortedByDescending { it.order }.map { CancellationTarget(it.id.toString(), it.nickname, it.quantity, it.heldAt) },
                product.candidates.associate { it.id.toString() to it.order },
            )
        }
        val suggested = result.products.flatMap { it.candidates }.filter { it.suggested }.take(100).map { it.id.toString() }.toSet()
        _uiState.value = PickupCancellationState(
            shortages = groups,
            excludedIds = groups.flatMap { it.targets }.map { it.id }.toSet() - suggested,
            noticeMessage = result.notice,
            suggestedCount = result.suggestedCount,
        )
    }

    fun handleAction(action: PickupCancellationAction) {
        when (action) {
            PickupCancellationAction.Refresh -> refresh()
            is PickupCancellationAction.SelectionChanged -> {
                if (uiState.value.isLoading || uiState.value.isSaving || uiState.value.showConfirmation || uiState.value.targets.none { it.id == action.id }) return
                if (action.selected && uiState.value.selectedIds.size >= 100) return
                _uiState.update { it.copy(excludedIds = if (action.selected) it.excludedIds - action.id else it.excludedIds + action.id) }
            }
            PickupCancellationAction.CancelClicked -> {
                val state = uiState.value
                if (state.isLoading || state.isSaving || state.hasError || state.selectedIds.size !in 1..100) return
                _uiState.update { it.copy(showConfirmation = true, confirmationIds = it.selectedIds) }
            }
            PickupCancellationAction.ConfirmationDismissed -> if (!uiState.value.isSaving) _uiState.update { it.copy(showConfirmation = false) }
            PickupCancellationAction.ConfirmationClicked -> {
                val state = uiState.value
                if (!state.showConfirmation || state.isSaving || state.confirmationIds.size !in 1..100) return
                _uiState.update { it.copy(isSaving = true, hasError = false) }
                viewModelScope.launch {
                    repository.cancelHolds(state.confirmationIds.map { it.toLong() }.toSet()).first().fold(
                        onSuccess = ::show,
                        onFailure = {
                            // 409 또는 응답 유실 시에도 이전 선택을 자동 재전송하지 않는다.
                            val latest = repository.fetchCancellations().first().getOrNull()
                            if (latest != null) show(latest)
                            _uiState.update { it.copy(isSaving = false, showConfirmation = false, hasError = true) }
                        },
                    )
                }
            }
            PickupCancellationAction.NavigationBackClicked -> if (!uiState.value.isSaving) {
                if (uiState.value.showConfirmation) {
                    _uiState.update { it.copy(showConfirmation = false) }
                } else {
                    _event.trySend(PickupCancellationEvent.NavigateBack)
                }
            }
        }
    }
}
