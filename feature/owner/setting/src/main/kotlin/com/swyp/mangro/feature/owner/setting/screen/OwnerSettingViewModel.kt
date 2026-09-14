package com.swyp.mangro.feature.owner.setting.screen

import androidx.lifecycle.ViewModel
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.feature.owner.setting.data.initialOwnerSettingState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class OwnerSettingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(initialOwnerSettingState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<OwnerSettingEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun handleAction(action: OwnerSettingAction) {
        when (action) {
            is OwnerSettingAction.MenuClicked -> when (action.menu) {
                OwnerMenu.HOME -> _event.trySend(OwnerSettingEvent.NavigateToHome)
                OwnerMenu.STORE -> _event.trySend(OwnerSettingEvent.NavigateToProducts)
                OwnerMenu.SETTINGS -> Unit
            }
            is OwnerSettingAction.PolicyClicked -> _event.trySend(OwnerSettingEvent.NavigateToPolicy(action.policy))
            OwnerSettingAction.NavigationBackClicked -> _event.trySend(OwnerSettingEvent.NavigateToHome)
        }
    }
}
