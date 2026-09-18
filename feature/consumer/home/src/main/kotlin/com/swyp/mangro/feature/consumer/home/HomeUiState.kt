package com.swyp.mangro.feature.consumer.home

import androidx.compose.runtime.Composable
import com.swyp.mangro.core.designsystem.component.StorePinState
import com.swyp.mangro.core.model.product.Product
import com.swyp.mangro.core.model.product.ProductCategory

data class HomeUiState(
    val locationName: String = "",
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val viewMode: HomeViewMode = HomeViewMode.MAP,
    val isLocationPermissionGranted: Boolean = false,
    val storePins: List<StorePinMarker> = emptyList(),
    val selectedStore: SelectedStoreDetail? = null,
    val activeWish: ActiveWishSummary? = null,
    val selectedCategory: ProductCategory? = null,
    val storeGroups: List<StoreProductGroup> = emptyList(),
    val sortOption: HomeSortOption = HomeSortOption.DISTANCE,
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
    val products: List<com.swyp.mangro.core.designsystem.component.card.map.StoreProduct>,
)

data class ActiveWishSummary(
    val holdId: String,
    val storeName: String,
    val productSummary: String,
    val requestTimeMillis: Long,
    val endTimeMillis: Long,
)

data class StoreProductGroup(
    val storeId: String,
    val storeName: String,
    val walkingMinutes: Int,
    val closingInMinutes: Int?,
    val products: List<Product>,
)

enum class HomeSortOption {
    DISTANCE,
    DEADLINE,
}

@Composable
fun HomeSortOption.toLabelRes(): Int = when (this) {
    HomeSortOption.DISTANCE -> R.string.home_sort_distance
    HomeSortOption.DEADLINE -> R.string.home_sort_deadline
}
