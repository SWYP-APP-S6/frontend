package com.swyp.mangro.feature.consumer.hold.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.feature.consumer.hold.navigation.HoldDetailDestination
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
class HoldDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val holdId: String = savedStateHandle.toRoute<HoldDetailDestination>().holdId

    private val _uiState = MutableStateFlow(HoldDetailUiState())
    val uiState: StateFlow<HoldDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HoldDetailUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadHoldDetail(holdId)
    }

    fun handleAction(action: HoldDetailUiAction) {
        when (action) {
            is HoldDetailUiAction.OnCancelClick -> {
                viewModelScope.launch {
                    _uiEvent.send(HoldDetailUiEvent.NavigateToHoldHistory)
                }
            }
        }
    }

    private fun loadHoldDetail(holdId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _uiState.update {
                dummyHoldDetailUiState.copy(isLoading = false)
            }
        }
    }
}
