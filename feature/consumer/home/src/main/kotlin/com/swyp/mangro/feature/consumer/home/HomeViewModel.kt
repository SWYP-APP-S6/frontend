package com.swyp.mangro.feature.consumer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.storePinStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            locationName = "망원동",
            storePins = listOf(
                StorePinMarker(
                    storeId = "dummy-1",
                    latitude = 37.5563,
                    longitude = 126.9099,
                    pinState = storePinStateOf(count = 3, name = "청과마을", isSelected = false),
                ),
                StorePinMarker(
                    storeId = "dummy-2",
                    latitude = 37.5570,
                    longitude = 126.9110,
                    pinState = storePinStateOf(count = 5, name = "야채가게", isSelected = false),
                ),
                StorePinMarker(
                    storeId = "dummy-3",
                    latitude = 37.5550,
                    longitude = 126.9080,
                    pinState = storePinStateOf(count = 0, name = "정육점", isSelected = false),
                ),
            ),
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _event = Channel<HomeUiEvent>()
    val event = _event.receiveAsFlow()

    fun handleAction(action: HomeUiAction) {
        when (action) {
            HomeUiAction.PermissionBannerActionClicked -> {
                viewModelScope.launch { _event.send(HomeUiEvent.RequestLocationPermission) }
            }
            is HomeUiAction.ViewModeChanged -> {
                _uiState.update { it.copy(viewMode = action.mode) }
            }
            is HomeUiAction.StorePinClicked -> {
                // TODO: selectedStore 갱신
            }
            HomeUiAction.SelectedStoreDismissed -> {
                _uiState.update { it.copy(selectedStore = null) }
            }
            is HomeUiAction.ProductClicked -> {
                viewModelScope.launch {
                    _event.send(HomeUiEvent.NavigateToProductDetail(action.product.id))
                }
            }
            is HomeUiAction.BottomMenuClicked -> {
                when (action.menu) {
                    ConsumerMenu.HOME -> Unit
                    ConsumerMenu.WISH_LIST -> {
                        viewModelScope.launch { _event.send(HomeUiEvent.NavigateToWishList) }
                    }
                    ConsumerMenu.MY -> {
                        viewModelScope.launch { _event.send(HomeUiEvent.NavigateToMy) }
                    }
                }
            }
        }
    }

    fun updateLocationPermission(isGranted: Boolean) {
        _uiState.update { it.copy(isLocationPermissionGranted = isGranted) }
    }
}

private fun MutableStateFlow<HomeUiState>.update(block: (HomeUiState) -> HomeUiState) {
    value = block(value)
}
