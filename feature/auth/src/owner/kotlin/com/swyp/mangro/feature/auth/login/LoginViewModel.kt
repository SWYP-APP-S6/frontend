package com.swyp.mangro.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.owner.auth.AuthResult
import com.swyp.mangro.data.owner.auth.OwnerAuthRepository
import com.swyp.mangro.data.owner.auth.OwnerSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: OwnerAuthRepository) : ViewModel() {
    private val state = MutableStateFlow(LoginUiState())
    val uiState = state.asStateFlow()
    private val events = Channel<LoginUiEvent>(Channel.BUFFERED)
    val event = events.receiveAsFlow()
    private var exchanging = false

    fun handleAction(action: LoginUiAction) {
        when (action) {
            LoginUiAction.KakaoLoginClicked -> if (!state.value.isLoading) {
                state.value = LoginUiState(isLoading = true)
                events.trySend(LoginUiEvent.LaunchKakaoLogin)
            }
            LoginUiAction.PrivacyPolicyClicked -> if (!state.value.isLoading) events.trySend(LoginUiEvent.NavigateToPrivacyPolicy)
            is LoginUiAction.KakaoLoginFailed -> if (!exchanging) {
                state.value = LoginUiState(hasError = !action.cancelled)
            }
            is LoginUiAction.KakaoTokenReceived -> if (state.value.isLoading && !exchanging) {
                exchanging = true
                viewModelScope.launch {
                    when (val result = repository.login(action.token)) {
                        is AuthResult.Failure -> state.value = LoginUiState(hasError = true)
                        is AuthResult.Success -> {
                            state.value = LoginUiState()
                            events.send(if (result.value == OwnerSession.SIGNUP_REQUIRED) LoginUiEvent.NavigateToTerms else LoginUiEvent.NavigateToHome)
                        }
                    }
                    exchanging = false
                }
            }
        }
    }
}
