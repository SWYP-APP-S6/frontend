package com.swyp.mangro.feature.consumer.home

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.card.map.StoreProduct
import com.swyp.mangro.core.designsystem.component.count
import com.swyp.mangro.core.designsystem.component.storePinStateOf
import com.swyp.mangro.core.model.product.Product
import com.swyp.mangro.core.model.product.ProductCategory
import com.swyp.mangro.core.utils.LocationProvider
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.consumer.home.model.ProductSortOption
import com.swyp.mangro.data.consumer.home.repository.ConsumerHomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"
private const val DEFAULT_BOUNDS_DELTA = 0.01

private const val EXPANDED_RADIUS_METERS = 5_000

private const val EXPANDED_LAT_DELTA = 0.045
private const val EXPANDED_LNG_DELTA = 0.057
private const val DEFAULT_MAP_ZOOM = 15.0
private const val LOCATION_REFRESH_THRESHOLD_METERS = 100f
private const val EXPANDED_MAP_ZOOM = 12.0

// TODO: 권한 없이 둘러볼 때 지도 중심 좌표 (기획 확인 필요, 임시로 서울시청)
private const val FALLBACK_LATITUDE = 37.5666
private const val FALLBACK_LONGITUDE = 126.9784
private const val FALLBACK_REGION_NAME = "내 위치"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ConsumerHomeRepository,
    private val authRepository: AuthRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _event = Channel<HomeUiEvent>()
    val event = _event.receiveAsFlow()

    private var lastLat: Double? = null
    private var lastLng: Double? = null
    private var locationJob: Job? = null

    private var hasUserLocation = false

    private var searchRadiusMeters: Int? = null
    private var nearbyProductsJob: Job? = null

    private var isGuest: Boolean? = null
    private var lastIsGranted: Boolean? = null
    private var canShowIntro = false
    private var hasShownPermissionIntro = false

    init {
        loadActiveHold()
        loadSessionType()
    }

    private fun loadSessionType() {
        viewModelScope.launch {
            isGuest = runCatching { authRepository.isGuestSession().first() }.getOrDefault(true)
            applyLocationPermission()
        }
    }

    fun onLocationPermissionChecked(isGranted: Boolean, canShowIntro: Boolean = false) {
        lastIsGranted = isGranted
        if (canShowIntro) this.canShowIntro = true
        applyLocationPermission()
    }

    private fun applyLocationPermission() {
        val isGranted = lastIsGranted ?: return
        if (!isGranted && isGuest == null) return

        val shouldShowIntro = !isGranted && isGuest == false && canShowIntro && !hasShownPermissionIntro
        if (shouldShowIntro) hasShownPermissionIntro = true

        _uiState.update {
            it.copy(
                locationPermission = if (isGranted) {
                    LocationPermissionStatus.GRANTED
                } else {
                    LocationPermissionStatus.DENIED
                },
                isPermissionIntroVisible = !isGranted && (it.isPermissionIntroVisible || shouldShowIntro),
            )
        }
        if (isGranted && !hasUserLocation && locationJob?.isActive != true) {
            loadLocation()
        }
    }

    private fun browseWithoutLocation() {
        _uiState.update { it.copy(isBrowsingWithoutPermission = true, viewMode = HomeViewMode.MAP) }
        applyLocation(
            regionName = FALLBACK_REGION_NAME,
            latitude = FALLBACK_LATITUDE,
            longitude = FALLBACK_LONGITUDE,
            latDelta = EXPANDED_LAT_DELTA,
            lngDelta = EXPANDED_LNG_DELTA,
            zoom = EXPANDED_MAP_ZOOM,
            radiusMeters = EXPANDED_RADIUS_METERS,
        )
    }

    private fun expandRadius() {
        val lat = lastLat ?: return
        val lng = lastLng ?: return
        _uiState.update { it.copy(isNearbyProductsEmpty = false) }
        applyLocation(
            regionName = _uiState.value.locationName,
            latitude = lat,
            longitude = lng,
            latDelta = EXPANDED_LAT_DELTA,
            lngDelta = EXPANDED_LNG_DELTA,
            zoom = EXPANDED_MAP_ZOOM,
            radiusMeters = EXPANDED_RADIUS_METERS,
        )
    }

    private fun loadLocation() {
        locationJob = viewModelScope.launch {
            if (resolveLocationViaGps()) return@launch

            repository.fetchMyLocation().collect { result ->
                val saved = result.getOrNull() ?: return@collect
                hasUserLocation = true
                applyLocation(saved.regionName, saved.latitude, saved.longitude)
            }
        }
    }

    fun refreshOnResume() {
        val canRefreshLocation = hasUserLocation &&
            _uiState.value.isLocationPermissionGranted &&
            locationJob?.isActive != true

        if (!canRefreshLocation) {
            refreshNearbyProducts()
            return
        }
        locationJob = viewModelScope.launch {
            val lat = lastLat
            val lng = lastLng
            val deviceLocation = locationProvider.fetchCurrentLocation()
            val hasMoved = deviceLocation != null &&
                lat != null &&
                lng != null &&
                distanceMeters(lat, lng, deviceLocation.latitude, deviceLocation.longitude) >=
                LOCATION_REFRESH_THRESHOLD_METERS

            if (deviceLocation != null && hasMoved) {
                updateDeviceLocation(deviceLocation.latitude, deviceLocation.longitude)
            } else {
                refreshNearbyProducts()
            }
        }
    }

    private fun onDeviceLocationChanged(latitude: Double, longitude: Double) {
        val lat = lastLat ?: return
        val lng = lastLng ?: return
        if (!hasUserLocation || locationJob?.isActive == true) return
        if (distanceMeters(lat, lng, latitude, longitude) < LOCATION_REFRESH_THRESHOLD_METERS) return

        locationJob = viewModelScope.launch {
            updateDeviceLocation(latitude, longitude, moveCamera = false)
        }
    }

    private suspend fun resolveLocationViaGps(): Boolean {
        val deviceLocation = locationProvider.fetchCurrentLocation() ?: return false
        updateDeviceLocation(deviceLocation.latitude, deviceLocation.longitude)
        return true
    }

    private suspend fun updateDeviceLocation(latitude: Double, longitude: Double, moveCamera: Boolean = true) {
        val currentRegionName = _uiState.value.locationName

        hasUserLocation = true
        if (moveCamera) {
            applyLocation(currentRegionName, latitude, longitude)
        } else {
            lastLat = latitude
            lastLng = longitude
            searchRadiusMeters = null
            refreshNearbyProducts()
        }

        val regionName = locationProvider.fetchRegionName(latitude, longitude) ?: "내 위치"
        _uiState.update { it.copy(locationName = regionName) }

        repository.setMyLocation(
            regionName = regionName,
            latitude = latitude,
            longitude = longitude,
        ).collect { result ->
            result.onFailure {
                // TODO: 내 위치 저장 실패 처리 (게스트는 무시)
            }
        }
    }

    private fun applyLocation(
        regionName: String,
        latitude: Double,
        longitude: Double,
        latDelta: Double = DEFAULT_BOUNDS_DELTA,
        lngDelta: Double = DEFAULT_BOUNDS_DELTA,
        zoom: Double = DEFAULT_MAP_ZOOM,
        radiusMeters: Int? = null,
    ) {
        lastLat = latitude
        lastLng = longitude
        searchRadiusMeters = radiusMeters
        _uiState.update {
            it.copy(
                locationName = regionName,
                locationLatitude = latitude,
                locationLongitude = longitude,
                mapZoom = zoom,
            )
        }
        loadNearbyStores(
            minLat = latitude - latDelta,
            maxLat = latitude + latDelta,
            minLng = longitude - lngDelta,
            maxLng = longitude + lngDelta,
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
            }
        }
    }

    fun refreshNearbyProducts() {
        val lat = lastLat ?: return
        val lng = lastLng ?: return
        loadNearbyProducts(lat, lng, _uiState.value.sortOption)
    }

    private fun loadNearbyProducts(lat: Double, lng: Double, sortOption: HomeSortOption) {
        if (!hasUserLocation) return
        nearbyProductsJob?.cancel()
        nearbyProductsJob = viewModelScope.launch {
            repository.fetchNearbyProducts(
                lat = lat,
                lng = lng,
                sort = sortOption.toRemoteSort(),
                radiusMeters = searchRadiusMeters,
            ).collect { result ->
                result
                    .onSuccess { nearbyProducts ->
                        _uiState.update { state ->
                            state.copy(
                                isNearbyProductsEmpty = nearbyProducts.storeGroups.all { it.products.isEmpty() } &&
                                    searchRadiusMeters != EXPANDED_RADIUS_METERS,
                                storeGroups = nearbyProducts.storeGroups.map { group ->
                                    StoreProductGroup(
                                        storeId = group.storeId.toString(),
                                        storeName = group.storeName,
                                        walkingMinutes = group.walkingMinutes,
                                        closingInMinutes = group.earliestPickupEndAtMillis?.toRemainingMinutes(),
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
                    .onFailure { error ->
                        // TODO: 주변 상품 조회 실패 처리
                        Log.e(TAG, "fetchNearbyProducts failed", error)
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
                                        holdId = it.holdId.toString(),
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
            HomeUiAction.PermissionIntroAllowClicked -> {
                _uiState.update { it.copy(isPermissionIntroVisible = false) }
                viewModelScope.launch {
                    _event.send(HomeUiEvent.RequestLocationPermission)
                }
            }
            HomeUiAction.PermissionIntroLaterClicked -> {
                _uiState.update { it.copy(isPermissionIntroVisible = false) }
            }
            HomeUiAction.PermissionBannerActionClicked -> {
                viewModelScope.launch {
                    _event.send(HomeUiEvent.RequestLocationPermission)
                }
            }
            HomeUiAction.BrowseWithoutLocationClicked -> browseWithoutLocation()
            HomeUiAction.ExpandRadiusClicked -> expandRadius()
            is HomeUiAction.ViewModeChanged -> {
                _uiState.update { it.copy(viewMode = action.mode) }
                if (action.mode == HomeViewMode.LIST) refreshNearbyProducts()
            }
            is HomeUiAction.StorePinClicked -> {
                selectStore(action.storeId)
            }
            HomeUiAction.SelectedStoreDismissed -> {
                _uiState.update { it.copy(selectedStore = null) }
            }
            is HomeUiAction.ProductClicked -> {
                _uiState.update { it.copy(selectedStore = null) }
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
            is HomeUiAction.DeviceLocationChanged -> onDeviceLocationChanged(action.latitude, action.longitude)
            is HomeUiAction.MapBoundsChanged -> {
                loadNearbyStores(action.minLat, action.maxLat, action.minLng, action.maxLng)
            }
            is HomeUiAction.ActiveWishClicked -> {
                viewModelScope.launch {
                    _event.send(HomeUiEvent.NavigateToHold(action.holdId))
                }
            }
        }
    }

    private fun selectStore(storeId: String) {
        val id = storeId.toLongOrNull() ?: return
        viewModelScope.launch {
            repository.fetchStoreProducts(
                storeId = id,
                lat = lastLat.takeIf { hasUserLocation },
                lng = lastLng.takeIf { hasUserLocation },
            ).collect { result ->
                result
                    .onSuccess { detail ->
                        _uiState.update { state ->
                            state.copy(
                                selectedStore = SelectedStoreDetail(
                                    storeId = storeId,
                                    storeName = detail.name,
                                    closingTime = (detail.businessCloseTime ?: "").toHourMinuteOrEmpty(),
                                    walkingMinutes = detail.walkingMinutes,
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
                                        pin.copy(
                                            pinState = storePinStateOf(
                                                count = pin.pinState.count,
                                                name = detail.name,
                                                isSelected = true,
                                            ),
                                        )
                                    } else {
                                        pin.copy(
                                            pinState = storePinStateOf(
                                                count = pin.pinState.count,
                                                name = "",
                                                isSelected = false,
                                            ),
                                        )
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
}

private fun HomeSortOption.toRemoteSort(): ProductSortOption = when (this) {
    HomeSortOption.DISTANCE -> ProductSortOption.DISTANCE
    HomeSortOption.DEADLINE -> ProductSortOption.PICKUP_DEADLINE
}

private fun MutableStateFlow<HomeUiState>.update(block: (HomeUiState) -> HomeUiState) {
    value = block(value)
}

private fun distanceMeters(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): Float {
    val result = FloatArray(1)
    Location.distanceBetween(fromLat, fromLng, toLat, toLng, result)
    return result[0]
}

private fun Long.toRemainingMinutes(): Int = ((this - System.currentTimeMillis()) / 60_000L).toInt().coerceAtLeast(0)

private fun String.toHourMinuteOrEmpty(): String = takeIf { it.isNotBlank() }
    ?.split(":")
    ?.take(2)
    ?.joinToString(":")
    ?: this
