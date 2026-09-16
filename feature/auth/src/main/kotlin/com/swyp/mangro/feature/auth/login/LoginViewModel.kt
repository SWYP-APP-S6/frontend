package com.swyp.mangro.feature.auth.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.LoginStatus
import com.swyp.mangro.data.auth.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val kakaoLoginLauncher: KakaoLoginLauncher,
) : ViewModel() {
    private var isVerifyingToken = false

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _event = Channel<LoginUiEvent>()
    val event = _event.receiveAsFlow()

    fun launchKakaoLogin(context: Context) {
        kakaoLoginLauncher.login(context) { result, accessToken ->
            handleAction(LoginUiAction.KakaoLoginCompleted(result, accessToken))
        }
    }

    fun handleAction(action: LoginUiAction) {
        when (action) {
            LoginUiAction.KakaoLoginClicked -> {
                if (_uiState.value.isLoading) return

                _uiState.update { it.copy(isLoading = true) }
                viewModelScope.launch { _event.send(LoginUiEvent.LaunchKakaoLogin) }
            }

            is LoginUiAction.KakaoLoginCompleted -> {
                if (!_uiState.value.isLoading || isVerifyingToken) return
                if (action.result != KakaoLoginResult.Success) {
                    _uiState.update { it.copy(isLoading = false) }
                    viewModelScope.launch { _event.send(LoginUiEvent.ShowKakaoLoginResult(action.result)) }
                    return
                }
                isVerifyingToken = true
                viewModelScope.launch {
                    try {
                        authRepository.login(action.accessToken.orEmpty()).collect { result ->
                            when (result) {
                                is AuthResult.Success -> _event.send(
                                    when (result.value) {
                                        LoginStatus.AUTHENTICATED -> LoginUiEvent.NavigateToHome
                                        LoginStatus.SIGNUP_REQUIRED -> LoginUiEvent.NavigateToTerms
                                    },
                                )
                                is AuthResult.Failure -> _event.send(LoginUiEvent.ShowAuthFailure(result.reason))
                            }
                        }
                    } finally {
                        isVerifyingToken = false
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }

            LoginUiAction.BrowseWithoutLoginClicked -> {
                if (_uiState.value.isLoading) return
                viewModelScope.launch { _event.send(LoginUiEvent.NavigateToHome) }
            }

            LoginUiAction.PrivacyPolicyClicked -> {
                if (_uiState.value.isLoading) return
                viewModelScope.launch { _event.send(LoginUiEvent.NavigateToPrivacyPolicy) }
            }
        }
    }
}
