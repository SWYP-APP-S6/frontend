package com.swyp.mangro.feature.owner.setting.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OwnerSettingViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OwnerSettingUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<OwnerSettingEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            storeRepository
                .fetchMyStore()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, hasStoreError = false) }
                }
                .first()
                .fold(
                    onSuccess = { store -> _uiState.update { it.copy(storeName = store.name, storePhone = store.phone, isLoading = false) } },
                    onFailure = { _uiState.update { it.copy(isLoading = false, hasStoreError = true) } },
                )
        }
    }

    fun refresh() {
        if (uiState.value.isLoading || uiState.value.isLoggingOut) return

        viewModelScope.launch {
            storeRepository
                .fetchMyStore()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, hasStoreError = false) }
                }
                .first()
                .fold(
                    onSuccess = { store -> _uiState.update { it.copy(storeName = store.name, storePhone = store.phone, isLoading = false) } },
                    onFailure = { _uiState.update { it.copy(isLoading = false, hasStoreError = true) } },
                )
        }
    }

    private fun logout() {
        if (uiState.value.isLoggingOut || !uiState.value.showLogoutConfirmation) return
        _uiState.update { it.copy(isLoggingOut = true, showLogoutConfirmation = false, hasLogoutError = false) }
        viewModelScope.launch {
            when (authRepository.logout().first()) {
                is AuthResult.Success -> _event.send(OwnerSettingEvent.NavigateToLogin)
                is AuthResult.Failure -> _uiState.update { it.copy(isLoggingOut = false, hasLogoutError = true) }
            }
        }
    }

    fun handleAction(action: OwnerSettingAction) {
        if (uiState.value.isLoggingOut) return
        when (action) {
            OwnerSettingAction.Refresh -> refresh()

            OwnerSettingAction.LogoutClicked -> _uiState.update { it.copy(showLogoutConfirmation = true, hasLogoutError = false) }
            OwnerSettingAction.LogoutConfirmed -> logout()
            OwnerSettingAction.LogoutDismissed -> _uiState.update { it.copy(showLogoutConfirmation = false) }
            OwnerSettingAction.LogoutErrorDismissed -> _uiState.update { it.copy(hasLogoutError = false) }

            is OwnerSettingAction.MenuClicked -> when (action.menu) {
                OwnerMenu.HOME -> _event.trySend(OwnerSettingEvent.NavigateToHome)
                OwnerMenu.STORE -> _event.trySend(OwnerSettingEvent.NavigateToProducts)
                OwnerMenu.SETTINGS -> Unit
            }

            is OwnerSettingAction.PolicyClicked -> _event.trySend(OwnerSettingEvent.NavigateToPolicy(action.policy))

            OwnerSettingAction.NavigationBackClicked -> when {
                uiState.value.showLogoutConfirmation -> _uiState.update { it.copy(showLogoutConfirmation = false) }
                uiState.value.hasLogoutError -> _uiState.update { it.copy(hasLogoutError = false) }
                else -> _event.trySend(OwnerSettingEvent.NavigateToHome)
            }
        }
    }
}
