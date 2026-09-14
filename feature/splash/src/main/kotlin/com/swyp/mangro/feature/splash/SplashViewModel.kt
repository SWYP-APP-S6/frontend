package com.swyp.mangro.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {
    private val _event = Channel<SplashUiEvent>()
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            delay(1000.milliseconds)

            // TODO: 실제 로그인 여부 체크 로직으로 교체
            val isLoggedIn = false
            _event.send(if (isLoggedIn) SplashUiEvent.NavigateToHome else SplashUiEvent.NavigateToLogin)
        }
    }
}
