package com.swyp.mangro.feature.owner.setting.screen.policy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.swyp.mangro.feature.owner.setting.navigation.OwnerPolicyDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class OwnerPolicyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // Connect the confirmed policy URL here when it is available.
    private val _uiState = MutableStateFlow(
        OwnerPolicyUiState(policy = savedStateHandle.toRoute<OwnerPolicyDestination>().policy),
    )
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<OwnerPolicyEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun handleAction(action: OwnerPolicyAction) {
        when (action) {
            OwnerPolicyAction.NavigationBackClicked -> _event.trySend(OwnerPolicyEvent.NavigateBack)
        }
    }
}
