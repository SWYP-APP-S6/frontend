package com.swyp.mangro.feature.consumer.home

import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.card.map.StoreProduct
import com.swyp.mangro.core.model.product.ProductCategory

sealed interface HomeUiAction {
    data object PermissionIntroAllowClicked : HomeUiAction
    data object PermissionIntroLaterClicked : HomeUiAction
    data object PermissionBannerActionClicked : HomeUiAction
    data object BrowseWithoutLocationClicked : HomeUiAction
    data object ExpandRadiusClicked : HomeUiAction
    data class ViewModeChanged(val mode: HomeViewMode) : HomeUiAction
    data class StorePinClicked(val storeId: String) : HomeUiAction
    data object SelectedStoreDismissed : HomeUiAction
    data class SortOptionSelected(val option: HomeSortOption) : HomeUiAction
    data class ProductClicked(val product: StoreProduct) : HomeUiAction
    data class ListProductClicked(val productId: String) : HomeUiAction
    data class CategorySelected(val category: ProductCategory?) : HomeUiAction
    data class BottomMenuClicked(val menu: ConsumerMenu) : HomeUiAction
    data class DeviceLocationChanged(val latitude: Double, val longitude: Double) : HomeUiAction
    data class MapBoundsChanged(val minLat: Double, val maxLat: Double, val minLng: Double, val maxLng: Double) : HomeUiAction
    data class ActiveWishClicked(val holdId: String) : HomeUiAction
    data object NearbyProductsRetryClicked : HomeUiAction
}
