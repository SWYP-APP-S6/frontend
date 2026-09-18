package com.swyp.mangro.feature.consumer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.card.map.StoreProduct
import com.swyp.mangro.core.designsystem.component.count
import com.swyp.mangro.core.designsystem.component.storePinStateOf
import com.swyp.mangro.core.model.product.Product
import com.swyp.mangro.core.model.product.ProductCategory
import com.swyp.mangro.core.utils.LocationProvider
import com.swyp.mangro.data.consumer.home.model.ProductSortOption
import com.swyp.mangro.data.consumer.home.repository.ConsumerHomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val DEFAULT_BOUNDS_DELTA = 0.01

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ConsumerHomeRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _event = Channel<HomeUiEvent>()
    val event = _event.receiveAsFlow()

    private var lastLat: Double? = null
    private var lastLng: Double? = null

    init {
        loadInitialLocationAndStores()
        loadActiveHold()
    }

    private fun loadInitialLocationAndStores() {
        viewModelScope.launch {
            repository.fetchMyLocation().collect { result ->
                result
                    .onSuccess { location ->
                        if (location == null) {
                            resolveLocationViaGps()
                            return@onSuccess
                        }
                        applyLocation(location.regionName, location.latitude, location.longitude)
                    }
                    .onFailure {
                        // TODO: 실패 처리
                    }
            }
        }
    }

    private fun resolveLocationViaGps() {
        if (!_uiState.value.isLocationPermissionGranted) {
            viewModelScope.launch { _event.send(HomeUiEvent.RequestLocationPermission) }
            return
        }
        viewModelScope.launch {
            val deviceLocation = locationProvider.fetchCurrentLocation()
            if (deviceLocation == null) {
                return@launch
            }
            val regionName = locationProvider.fetchRegionName(deviceLocation.latitude, deviceLocation.longitude)
                ?: "내 위치"

            repository.setMyLocation(
                regionName = regionName,
                latitude = deviceLocation.latitude,
                longitude = deviceLocation.longitude,
            ).collect { result ->
                result
                    .onSuccess { saved -> applyLocation(saved.regionName, saved.latitude, saved.longitude) }
                    .onFailure {
                        // TODO: 실패 처리
                    }
            }
        }
    }

    private fun applyLocation(regionName: String, latitude: Double, longitude: Double) {
        lastLat = latitude
        lastLng = longitude
        _uiState.update {
            it.copy(
                locationName = regionName,
                locationLatitude = latitude,
                locationLongitude = longitude,
                isLocationPermissionGranted = true,
            )
        }
        loadNearbyStores(
            minLat = latitude - DEFAULT_BOUNDS_DELTA,
            maxLat = latitude + DEFAULT_BOUNDS_DELTA,
            minLng = longitude - DEFAULT_BOUNDS_DELTA,
            maxLng = longitude + DEFAULT_BOUNDS_DELTA,
        )
        loadNearbyProducts(latitude, longitude, _uiState.value.sortOption)
    }

    private fun loadNearbyStores(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double) {
        viewModelScope.launch {
            repository.fetchNearbyStores(minLat, maxLat, minLng, maxLng).collect { result ->
                result
                    .onSuccess { nearby ->
                        _uiState.update { state ->
                            state.copy(
                                storePins = nearby.stores.map { store ->
                                    StorePinMarker(
                                        storeId = store.storeId.toString(),
                                        latitude = store.latitude,
                                        longitude = store.longitude,
                                        pinState = storePinStateOf(
                                            count = store.sellableProductCount,
                                            name = store.name,
                                            isSelected = false,
                                        ),
                                    )
                                },
                            )
                        }
                    }
                    .onFailure {
                        // TODO: 실패 처리
                    }
            }
        }
    }

    private fun loadNearbyProducts(lat: Double, lng: Double, sortOption: HomeSortOption) {
        viewModelScope.launch {
            repository.fetchNearbyProducts(
                lat = lat,
                lng = lng,
                sort = sortOption.toRemoteSort(),
            ).collect { result ->
                result
                    .onSuccess { nearbyProducts ->
                        _uiState.update { state ->
                            state.copy(
                                storeGroups = nearbyProducts.storeGroups.map { group ->
                                    StoreProductGroup(
                                        storeId = group.storeId.toString(),
                                        storeName = group.storeName,
                                        walkingMinutes = group.walkingMinutes,
                                        closingInMinutes = null, // TODO: earliestPickupEndAtMillis로부터 남은 분 계산
                                        products = group.products.map { product ->
                                            Product(
                                                id = product.id.toString(),
                                                imageUrl = product.photoUrl,
                                                discountRate = product.discountRate,
                                                name = product.name,
                                                price = product.salePrice,
                                                originalPrice = product.originalPrice.takeIf { it != product.salePrice },
                                                category = ProductCategory.fromStoreCategory(product.category),
                                                remainingCount = product.availableQty,
                                            )
                                        },
                                    )
                                },
                            )
                        }
                    }
                    .onFailure {
                        // TODO: 주변 상품 조회 실패 처리
                    }
            }
        }
    }

    private fun loadActiveHold() {
        viewModelScope.launch {
            repository.fetchActiveHold().collect { result ->
                result
                    .onSuccess { hold ->
                        _uiState.update { state ->
                            state.copy(
                                activeWish = hold?.let {
                                    ActiveWishSummary(
                                        storeName = it.storeName,
                                        productSummary = "${it.firstItemName} · ${it.totalQty}개",
                                        requestTimeMillis = it.heldAtMillis,
                                        endTimeMillis = it.expiresAtMillis,
                                    )
                                },
                            )
                        }
                    }
                    .onFailure {
                        // TODO: 활성 찜 조회 실패 처리
                    }
            }
        }
    }

    fun handleAction(action: HomeUiAction) {
        when (action) {
            HomeUiAction.PermissionBannerActionClicked -> {
                viewModelScope.launch {
                    _event.send(HomeUiEvent.RequestLocationPermission)
                }
            }
            HomeUiAction.ExpandRadiusClicked -> {
                // TODO: 반경 확장 후 재검색
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
                _uiState.update { it.copy(sortOption = action.option) }
                val lat = lastLat
                val lng = lastLng
                if (lat != null && lng != null) {
                    loadNearbyProducts(lat, lng, action.option)
                }
            }
            is HomeUiAction.MapBoundsChanged -> {
                loadNearbyStores(action.minLat, action.maxLat, action.minLng, action.maxLng)
            }
        }
    }

    private fun selectStore(storeId: String) {
        val id = storeId.toLongOrNull() ?: return
        viewModelScope.launch {
            repository.fetchStoreProducts(id, lastLat, lastLng).collect { result ->
                result
                    .onSuccess { detail ->
                        _uiState.update { state ->
                            state.copy(
                                selectedStore = SelectedStoreDetail(
                                    storeId = storeId,
                                    storeName = detail.name,
                                    closingTime = detail.businessCloseTime?.toHourMinute() ?: "",
                                    walkingMinutes = detail.walkingMinutes ?: 0,
                                    products = detail.products.map { product ->
                                        StoreProduct(
                                            id = product.id.toString(),
                                            imageUrl = product.photoUrl,
                                            discountRate = product.discountRate,
                                            productName = product.name,
                                            price = product.salePrice,
                                        )
                                    },
                                ),
                                storePins = state.storePins.map { pin ->
                                    if (pin.storeId == storeId) {
                                        pin.copy(pinState = storePinStateOf(count = pin.pinState.count, name = detail.name, isSelected = true))
                                    } else {
                                        pin.copy(pinState = storePinStateOf(count = pin.pinState.count, name = "", isSelected = false))
                                    }
                                },
                            )
                        }
                    }
                    .onFailure {
                        // TODO: 상점 상세 조회 실패 처리
                    }
            }
        }
    }

    fun updateLocationPermission(isGranted: Boolean) {
        _uiState.update { it.copy(isLocationPermissionGranted = isGranted) }
        if (isGranted) {
            resolveLocationViaGps()
        }
    }
}

private fun HomeSortOption.toRemoteSort(): ProductSortOption = when (this) {
    HomeSortOption.DISTANCE -> ProductSortOption.DISTANCE
    HomeSortOption.DEADLINE -> ProductSortOption.PICKUP_DEADLINE
}

private fun MutableStateFlow<HomeUiState>.update(block: (HomeUiState) -> HomeUiState) {
    value = block(value)
}

private fun String.toHourMinute(): String = takeIf { it.isNotBlank() }
    ?.split(":")
    ?.take(2)
    ?.joinToString(":")
    ?: this
