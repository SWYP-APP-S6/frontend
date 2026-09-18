package com.swyp.mangro.feature.consumer.myinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.user.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MyInfoViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyInfoUiState())
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<MyInfoUiEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, hasProfileError = false) }
        viewModelScope.launch {
            try {
                val isGuest = !authRepository.hasSession().first()
                _uiState.update { it.copy(isGuest = isGuest) }

                if (isGuest) {
                    _uiState.update { it.copy(isLoading = false, nickname = "", phone = "") }
                } else {
                    userRepository
                        .fetchMe()
                        .collect { result ->
                            when {
                                result.isSuccess -> {
                                    val profile = result.getOrNull()

                                    _uiState.update {
                                        it.copy(isLoading = false, nickname = profile?.nickname ?: "", phone = profile?.phone ?: "")
                                    }
                                }

                                result.isFailure -> {
                                    _uiState.update { it.copy(isLoading = false, hasProfileError = true) }
                                }
                            }
                        }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, hasProfileError = true) }
            }
        }
    }

    fun handleAction(action: MyInfoUiAction) {
        if (uiState.value.isLoggingOut) return
        when (action) {
            MyInfoUiAction.RetryClicked -> if (!uiState.value.isLoading) loadProfile()

            MyInfoUiAction.LinkKakaoClicked -> if (!uiState.value.isLoading && uiState.value.isGuest) {
                _event.trySend(MyInfoUiEvent.NavigateToLogin)
            }

            MyInfoUiAction.LogoutClicked -> if (!uiState.value.isLoading && !uiState.value.isGuest) {
                _uiState.update { it.copy(showLogoutConfirmation = true, hasLogoutError = false) }
            }

            MyInfoUiAction.LogoutDismissed -> _uiState.update { it.copy(showLogoutConfirmation = false) }

            MyInfoUiAction.LogoutErrorDismissed -> _uiState.update { it.copy(hasLogoutError = false) }

            MyInfoUiAction.LogoutConfirmed -> logout()

            is MyInfoUiAction.MenuClicked -> _event.trySend(MyInfoUiEvent.NavigateToMenu(action.menu))

            is MyInfoUiAction.PolicyClicked -> _event.trySend(MyInfoUiEvent.NavigateToPolicy(action.kind))
        }
    }

    private fun logout() {
        if (!uiState.value.showLogoutConfirmation || uiState.value.isGuest) return
        _uiState.update { it.copy(isLoggingOut = true, showLogoutConfirmation = false, hasLogoutError = false) }
        viewModelScope.launch {
            when (authRepository.logout().first()) {
                is AuthResult.Success -> _event.send(MyInfoUiEvent.NavigateToLogin)
                is AuthResult.Failure -> _uiState.update { it.copy(isLoggingOut = false, hasLogoutError = true) }
            }
        }
    }
}
