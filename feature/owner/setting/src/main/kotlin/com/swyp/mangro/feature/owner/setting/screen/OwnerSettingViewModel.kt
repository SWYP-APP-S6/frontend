package com.swyp.mangro.feature.owner.setting.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.data.user.repository.UserRepository
import com.swyp.mangro.feature.owner.setting.model.SettingsMenu
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OwnerSettingViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
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

    private fun logout() {
        if (uiState.value.isLoggingOut) return
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            when (authRepository.logout().first()) {
                is AuthResult.Success -> _event.send(OwnerSettingEvent.NavigateToLogin)

                is AuthResult.Failure -> {
                    _uiState.update { it.copy(isLoggingOut = false) }
                    _event.send(OwnerSettingEvent.ShowLogoutErrorDialog)
                }
            }
        }
    }

    private fun withdraw() {
        viewModelScope.launch {
            userRepository.withdrawUser()
                .catch {
                    _event.send(OwnerSettingEvent.ShowWithdrawErrorDialog)
                }.collect {
                    _event.send(OwnerSettingEvent.NavigateToLogin)
                }
        }
    }

    fun handleAction(action: OwnerSettingAction) {
        if (uiState.value.isLoggingOut) return
        when (action) {
            OwnerSettingAction.LogoutConfirmed -> logout()

            OwnerSettingAction.WithdrawConfirmed -> withdraw()

            is OwnerSettingAction.NavigationMenuClicked -> {
                when (action.menu) {
                    OwnerMenu.HOME -> _event.trySend(OwnerSettingEvent.NavigateToHome)
                    OwnerMenu.STORE -> _event.trySend(OwnerSettingEvent.NavigateToProducts)
                    OwnerMenu.SETTINGS -> Unit
                }
            }

            is OwnerSettingAction.SettingsMenuClicked -> {
                when (action.menu) {
                    SettingsMenu.TERMS_OF_SERVICE, SettingsMenu.PRIVACY_POLICY -> {
                        _event.trySend(OwnerSettingEvent.NavigateToPolicy(action.menu))
                    }

                    SettingsMenu.LOGOUT -> {
                        _event.trySend(OwnerSettingEvent.ShowLogoutConfirmDialog)
                    }

                    SettingsMenu.WITHDRAW -> {
                        _event.trySend(OwnerSettingEvent.ShowWithdrawConfirmDialog)
                    }
                }
            }

            OwnerSettingAction.NavigationBackClicked -> {
                _event.trySend(OwnerSettingEvent.NavigateToHome)
            }
        }
    }
}
