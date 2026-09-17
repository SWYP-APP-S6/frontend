package com.swyp.mangro.feature.consumer.hold.complete

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
class PickupCompleteViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(PickupCompleteUiState())
    val uiState: StateFlow<PickupCompleteUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<PickupCompleteUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadPickupComplete()
    }

    fun handleAction(action: PickupCompleteUiAction) {
        when (action) {
            is PickupCompleteUiAction.OnRecipeClick -> {
                viewModelScope.launch {
                    _uiEvent.send(PickupCompleteUiEvent.NavigateToRecipeDetail(action.recipeId))
                }
            }
            is PickupCompleteUiAction.OnMenuClick -> {
                viewModelScope.launch {
                    _uiEvent.send(PickupCompleteUiEvent.NavigateToMenu(action.menu))
                }
            }
        }
    }

    private fun loadPickupComplete() {
        viewModelScope.launch {
            _uiState.update {
                dummyPickupCompleteUiState
            }
        }
    }
}
