package com.swyp.mangro.feature.consumer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.card.map.StoreProduct
import com.swyp.mangro.core.designsystem.component.card.product.Product
import com.swyp.mangro.core.designsystem.component.card.product.ProductCategory
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

private val dummyStoreGroups = listOf(
    StoreProductGroup(
        storeId = "dummy-1",
        storeName = "청과마을",
        walkingMinutes = 7,
        closingInMinutes = 60,
        products = listOf(
            Product(
                id = "1",
                imageUrl = "",
                discountRate = 60,
                name = "복숭아 4입",
                price = 4_000,
                originalPrice = 10_000,
                category = ProductCategory.VEGETABLES,
                remainingCount = 3,
            ),
            Product(
                id = "2",
                imageUrl = "",
                discountRate = 50,
                name = "알배기 배추 2통",
                price = 3_000,
                originalPrice = 6_000,
                category = ProductCategory.VEGETABLES,
                remainingCount = 4,
            ),
            Product(
                id = "3",
                imageUrl = "",
                discountRate = null,
                name = "대파 1단",
                price = 3_500,
                originalPrice = null,
                category = ProductCategory.VEGETABLES,
                remainingCount = 2,
            ),
        ),
    ),
    StoreProductGroup(
        storeId = "dummy-2",
        storeName = "야채가게",
        walkingMinutes = 4,
        closingInMinutes = 30,
        products = listOf(
            Product(
                id = "4",
                imageUrl = "",
                discountRate = 30,
                name = "시금치 1단",
                price = 2_000,
                originalPrice = null,
                category = ProductCategory.VEGETABLES,
                remainingCount = 4,
            ),
        ),
    ),
    StoreProductGroup(
        storeId = "dummy-3",
        storeName = "정육점",
        walkingMinutes = 10,
        closingInMinutes = null,
        products = listOf(
            Product(
                id = "5",
                imageUrl = "",
                discountRate = 20,
                name = "돼지고기 300g",
                price = 6_000,
                originalPrice = 7_500,
                category = ProductCategory.MEAT,
                remainingCount = 5,
            ),
            Product(
                id = "6",
                imageUrl = "",
                discountRate = null,
                name = "닭가슴살 500g",
                price = 5_000,
                originalPrice = null,
                category = ProductCategory.MEAT,
                remainingCount = 8,
            ),
        ),
    ),
    StoreProductGroup(
        storeId = "dummy-4",
        storeName = "동네수산",
        walkingMinutes = 3,
        closingInMinutes = 15,
        products = listOf(
            Product(
                id = "7",
                imageUrl = "",
                discountRate = 45,
                name = "고등어 2마리",
                price = 5_500,
                originalPrice = 10_000,
                category = ProductCategory.SEAFOOD,
                remainingCount = 1,
            ),
            Product(
                id = "8",
                imageUrl = "",
                discountRate = 25,
                name = "손질 오징어 2팩",
                price = 6_000,
                originalPrice = 8_000,
                category = ProductCategory.SEAFOOD,
                remainingCount = 3,
            ),
        ),
    ),
    StoreProductGroup(
        storeId = "dummy-5",
        storeName = "곡물가게",
        walkingMinutes = 12,
        closingInMinutes = 120,
        products = listOf(
            Product(
                id = "9",
                imageUrl = "",
                discountRate = null,
                name = "백미 4kg",
                price = 15_000,
                originalPrice = null,
                category = ProductCategory.GRAINS,
                remainingCount = 6,
            ),
            Product(
                id = "10",
                imageUrl = "",
                discountRate = 15,
                name = "혼합 견과 300g",
                price = 9_000,
                originalPrice = 10_500,
                category = ProductCategory.NUTS,
                remainingCount = 4,
            ),
        ),
    ),
    StoreProductGroup(
        storeId = "dummy-6",
        storeName = "만물상회",
        walkingMinutes = 6,
        closingInMinutes = 45,
        products = listOf(
            Product(
                id = "11",
                imageUrl = "",
                discountRate = 10,
                name = "국산 참기름 1병",
                price = 12_000,
                originalPrice = 13_500,
                category = ProductCategory.ETC,
                remainingCount = 2,
            ),
        ),
    ),
)

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
                    pinState = storePinStateOf(count = 2, name = "정육점", isSelected = false),
                ),
            ),
            activeWish = ActiveWishSummary(
                storeName = "청과 마을",
                productSummary = "복숭아 4입 · 1개",
                requestTimeMillis = System.currentTimeMillis() - 5 * 60 * 1000,
                endTimeMillis = System.currentTimeMillis() + 9 * 60 * 1000 + 24 * 1000,
            ),
            storeGroups = dummyStoreGroups,
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _event = Channel<HomeUiEvent>()
    val event = _event.receiveAsFlow()

    fun handleAction(action: HomeUiAction) {
        when (action) {
            HomeUiAction.PermissionBannerActionClicked -> {
                // TODO: 실제 권한 요청
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
            is HomeUiAction.ListProductClicked -> {
                viewModelScope.launch {
                    _event.send(HomeUiEvent.NavigateToProductDetail(action.productId))
                }
            }
            is HomeUiAction.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = action.category) }
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

            is HomeUiAction.SortOptionSelected -> {
                _uiState.update { state ->
                    state.copy(
                        sortOption = action.option,
                        storeGroups = state.storeGroups.map { group ->
                            group.copy(
                                products = when (action.option) {
                                    HomeSortOption.DISTANCE -> group.products
                                    HomeSortOption.DEADLINE -> group.products
                                },
                            )
                        },
                    )
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
