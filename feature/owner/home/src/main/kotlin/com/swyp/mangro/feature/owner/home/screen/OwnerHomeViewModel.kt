package com.swyp.mangro.feature.owner.home.screen

import androidx.lifecycle.ViewModel
import com.swyp.mangro.feature.owner.home.OwnerHomeSamples
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class OwnerHomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(OwnerHomeSamples.operating())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<OwnerHomeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun handleAction(action: OwnerHomeAction) {
        when (action) {
            is OwnerHomeAction.CompletePickup -> {
            }

            OwnerHomeAction.ConfirmPickups -> {
            }

            OwnerHomeAction.DismissAttention -> {
                _uiState.update { it.copy(isAttentionDismissed = true) }
            }

            OwnerHomeAction.RegisterProduct -> {
                _uiState.update { it.copy(hasRegisteredProduct = !it.hasRegisteredProduct) }
            }

            OwnerHomeAction.ViewCancellations -> {
            }

            OwnerHomeAction.ViewCompletedPickups -> {
            }

            OwnerHomeAction.ViewNewPickups -> {
                _uiState.update { it.copy(hasNewPickup = !it.hasNewPickup) }
            }

            OwnerHomeAction.ViewNotifications -> {
            }

            is OwnerHomeAction.ViewPickup -> {
            }

            OwnerHomeAction.ViewPickups -> {
            }

            is OwnerHomeAction.ViewProduct -> {
            }

            OwnerHomeAction.ViewProducts -> {
            }
        }
    }
}
