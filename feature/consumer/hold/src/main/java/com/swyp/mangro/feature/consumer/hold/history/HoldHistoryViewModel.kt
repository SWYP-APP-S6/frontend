package com.swyp.mangro.feature.consumer.hold.history

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
class HoldHistoryViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(HoldHistoryUiState())
    val uiState: StateFlow<HoldHistoryUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HoldHistoryUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadHoldHistory()
    }

    fun handleAction(action: HoldHistoryUiAction) {
        when (action) {
            is HoldHistoryUiAction.OnItemClick -> {
                viewModelScope.launch {
                    _uiEvent.send(HoldHistoryUiEvent.NavigateToHoldDetail(action.id))
                }
            }

            is HoldHistoryUiAction.OnMenuClick -> {
                viewModelScope.launch {
                    _uiEvent.send(HoldHistoryUiEvent.NavigateToMenu(action.menu))
                }
            }
        }
    }

    private fun loadHoldHistory() {
        viewModelScope.launch {
            _uiState.update {
                dummyHoldHistoryUiState
            }
        }
    }
}
