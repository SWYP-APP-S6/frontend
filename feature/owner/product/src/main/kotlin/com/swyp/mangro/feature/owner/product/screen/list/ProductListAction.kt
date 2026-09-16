package com.swyp.mangro.feature.owner.product.screen.list

import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu

sealed interface ProductListAction {
    data object Refresh : ProductListAction
    data class TabSelected(val tab: ProductListTab) : ProductListAction
    data class FilterSelected(val filter: ProductListFilter) : ProductListAction
    data class ProductClicked(val id: String) : ProductListAction
    data class PickupClicked(val id: String) : ProductListAction
    data class PickupCompleteClicked(val id: String) : ProductListAction
    data class MenuSelected(val menu: OwnerMenu) : ProductListAction
    data object ReservationsCancelClicked : ProductListAction
}
