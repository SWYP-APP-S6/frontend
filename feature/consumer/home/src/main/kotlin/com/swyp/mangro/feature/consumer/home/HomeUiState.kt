package com.swyp.mangro.feature.consumer.home

import com.swyp.mangro.core.designsystem.component.StorePinState
import com.swyp.mangro.core.designsystem.component.card.map.StoreProduct

data class HomeUiState(
    val locationName: String = "",
    val viewMode: HomeViewMode = HomeViewMode.MAP,
    val isLocationPermissionGranted: Boolean = true,
    val storePins: List<StorePinMarker> = emptyList(),
    val selectedStore: SelectedStoreDetail? = null,
)

enum class HomeViewMode { MAP, LIST }

data class StorePinMarker(
    val storeId: String,
    val latitude: Double,
    val longitude: Double,
    val pinState: StorePinState,
)

data class SelectedStoreDetail(
    val storeId: String,
    val storeName: String,
    val closingTime: String,
    val walkingMinutes: Int,
    val products: List<StoreProduct>,
)
