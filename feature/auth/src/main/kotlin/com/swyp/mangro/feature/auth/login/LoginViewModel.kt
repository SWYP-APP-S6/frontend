package com.swyp.mangro.feature.auth.login

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
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _event = Channel<LoginUiEvent>()
    val event = _event.receiveAsFlow()

    fun handleAction(action: LoginUiAction) {
        when (action) {
            LoginUiAction.KakaoLoginClicked -> {
                // TODO: 카카오 로그인
                viewModelScope.launch { _event.send(LoginUiEvent.NavigateToHome) }
            }
            LoginUiAction.BrowseWithoutLoginClicked -> {
                viewModelScope.launch { _event.send(LoginUiEvent.NavigateToHome) }
            }
            LoginUiAction.PrivacyPolicyClicked -> {
                viewModelScope.launch { _event.send(LoginUiEvent.NavigateToPrivacyPolicy) }
            }
        }
    }
}
