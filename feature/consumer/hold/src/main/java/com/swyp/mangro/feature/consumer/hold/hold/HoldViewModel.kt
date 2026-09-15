package com.swyp.mangro.feature.consumer.hold.hold

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.card.timer.TimerCardPhase
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
class HoldViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
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
                    _uiEvent.send(HoldUiEvent.NavigateToProductDetail)
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
        }
    }

    private fun loadHoldInfo() {
        viewModelScope.launch {}
    }
}
