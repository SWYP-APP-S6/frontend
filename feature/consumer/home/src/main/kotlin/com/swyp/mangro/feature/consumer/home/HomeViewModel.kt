package com.swyp.mangro.feature.consumer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.card.map.StoreProduct
import com.swyp.mangro.core.designsystem.component.count
import com.swyp.mangro.core.designsystem.component.storePinStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private val dummyStoreDetails = mapOf(
    "dummy-1" to SelectedStoreDetail(
        storeId = "dummy-1",
        storeName = "청과마을",
        closingTime = "19:30",
        walkingMinutes = 7,
        products = listOf(
            StoreProduct(id = "1", imageUrl = "", discountRate = 60, productName = "복숭아 4입", price = 4_000),
            StoreProduct(id = "2", imageUrl = "", discountRate = 50, productName = "알배기 배추 2통", price = 3_000),
            StoreProduct(id = "3", imageUrl = "", discountRate = null, productName = "대파 1단", price = 3_500),
        ),
    ),
    "dummy-2" to SelectedStoreDetail(
        storeId = "dummy-2",
        storeName = "야채가게",
        closingTime = "20:00",
        walkingMinutes = 4,
        products = listOf(
            StoreProduct(id = "4", imageUrl = "", discountRate = 30, productName = "시금치 1단", price = 2_000),
        ),
    ),
    "dummy-3" to SelectedStoreDetail(
        storeId = "dummy-3",
        storeName = "정육점",
        closingTime = "18:00",
        walkingMinutes = 10,
        products = listOf(
            StoreProduct(id = "5", imageUrl = "", discountRate = 20, productName = "돼지고기 300g", price = 6_000),
        ),
    ),
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            locationName = "광화문",
            isLocationPermissionGranted = false,
            storePins = listOf(
                StorePinMarker(
                    storeId = "dummy-1",
                    latitude = 37.5759,
                    longitude = 126.9769,
                    pinState = storePinStateOf(count = 3, name = "청과마을", isSelected = false),
                ),
                StorePinMarker(
                    storeId = "dummy-2",
                    latitude = 37.5768,
                    longitude = 126.9780,
                    pinState = storePinStateOf(count = 5, name = "야채가게", isSelected = false),
                ),
                StorePinMarker(
                    storeId = "dummy-3",
                    latitude = 37.5748,
                    longitude = 126.9755,
                    pinState = storePinStateOf(count = 2, name = "정육점", isSelected = false),
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
                _uiState.update { it.copy(isLocationPermissionGranted = true) }
            }
            is HomeUiAction.ViewModeChanged -> {
                _uiState.update { it.copy(viewMode = action.mode) }
            }
            is HomeUiAction.StorePinClicked -> {
                selectStore(action.storeId)
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

    private fun selectStore(storeId: String) {
        val detail = dummyStoreDetails[storeId] ?: return

        _uiState.update { state ->
            state.copy(
                selectedStore = detail,
                storePins = state.storePins.map { pin ->
                    pin.copy(
                        pinState = storePinStateOf(
                            count = pin.pinState.count,
                            name = detail.storeName.takeIf { pin.storeId == storeId } ?: "",
                            isSelected = pin.storeId == storeId,
                        ),
                    )
                },
            )
        }
    }

    fun updateLocationPermission(isGranted: Boolean) {
        _uiState.update { it.copy(isLocationPermissionGranted = isGranted) }
    }
}

private fun MutableStateFlow<HomeUiState>.update(block: (HomeUiState) -> HomeUiState) {
    value = block(value)
}
