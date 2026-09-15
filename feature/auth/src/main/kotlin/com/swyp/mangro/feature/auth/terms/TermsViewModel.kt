package com.swyp.mangro.feature.auth.terms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TermsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TermsUiState())
    val uiState: StateFlow<TermsUiState> = _uiState.asStateFlow()

    private val _event = Channel<TermsUiEvent>()
    val event = _event.receiveAsFlow()

    fun handleAction(action: TermsUiAction) {
        when (action) {
            TermsUiAction.AllAgreeClicked -> toggleAll()
            is TermsUiAction.ItemToggled -> toggleItem(action.type)
            is TermsUiAction.ItemDetailClicked -> {
                viewModelScope.launch {
                    _event.send(TermsUiEvent.NavigateToTermsDetail(action.type))
                }
            }
            TermsUiAction.ConfirmClicked -> {
                viewModelScope.launch { _event.send(TermsUiEvent.NavigateToHome) }
            }
        }
    }

    private fun toggleAll() {
        val target = !_uiState.value.isAllChecked
        _uiState.update { state ->
            state.copy(items = state.items.map { it.copy(isChecked = target) })
        }
    }

    private fun toggleItem(type: TermsType) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map {
                    if (it.type == type) it.copy(isChecked = !it.isChecked) else it
                },
            )
        }
    }
}

private fun MutableStateFlow<TermsUiState>.update(block: (TermsUiState) -> TermsUiState) {
    value = block(value)
}
