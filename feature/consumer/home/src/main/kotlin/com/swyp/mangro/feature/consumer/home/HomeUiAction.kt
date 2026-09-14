package com.swyp.mangro.feature.consumer.home

import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.card.map.StoreProduct

sealed interface HomeUiAction {
    data object PermissionBannerActionClicked : HomeUiAction
    data class ViewModeChanged(val mode: HomeViewMode) : HomeUiAction
    data class StorePinClicked(val storeId: String) : HomeUiAction
    data object SelectedStoreDismissed : HomeUiAction
    data class ProductClicked(val product: StoreProduct) : HomeUiAction
    data class BottomMenuClicked(val menu: ConsumerMenu) : HomeUiAction
}
