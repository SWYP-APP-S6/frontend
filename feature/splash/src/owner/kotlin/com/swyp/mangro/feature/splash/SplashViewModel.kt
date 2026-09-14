package com.swyp.mangro.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.owner.auth.AuthFailure
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
class SplashViewModel @Inject constructor(private val auth: OwnerAuthRepository) : ViewModel() {
    private val events = Channel<SplashUiEvent>(Channel.BUFFERED)
    val event = events.receiveAsFlow()
    private val state = MutableStateFlow(SplashUiState())
    val uiState = state.asStateFlow()
    private var restoring = false

    init {
        retry()
    }

    fun handleAction(action: SplashUiAction) {
        when (action) {
            SplashUiAction.RetryClicked -> retry()
        }
    }

    private fun retry() {
        if (restoring) return
        restoring = true
        state.value = SplashUiState()
        viewModelScope.launch {
            when (val result = auth.restore()) {
                is AuthResult.Success -> events.send(if (result.value == OwnerSession.AUTHENTICATED) SplashUiEvent.NavigateToHome else SplashUiEvent.NavigateToLogin)
                is AuthResult.Failure -> if (result.reason == AuthFailure.UNAUTHORIZED) {
                    events.send(SplashUiEvent.NavigateToLogin)
                } else {
                    state.value = SplashUiState(isLoading = false, hasError = true)
                }
            }
            restoring = false
        }
    }
}
