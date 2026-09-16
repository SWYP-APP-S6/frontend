package com.swyp.mangro.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.auth.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val _event = Channel<SplashUiEvent>()
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            delay(1000.milliseconds)

            val isLoggedIn = try {
                repository.hasSession().first()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                false
            }
            _event.send(if (isLoggedIn) SplashUiEvent.NavigateToHome else SplashUiEvent.NavigateToLogin)
        }
    }
}
