package com.swyp.mangro.feature.owner.product.screen.list

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestStatus
import com.swyp.mangro.data.owner.product.model.OwnerProductFilter
import com.swyp.mangro.feature.owner.product.model.OwnerPickupModel
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel

enum class ProductListTab { PRODUCTS, PICKUPS }

enum class ProductListFilter(val status: OwnerPickupRequestStatus?) {
    ALL(null),
    COMPLETED(OwnerPickupRequestStatus.COMPLETED),
    UNAVAILABLE(OwnerPickupRequestStatus.UNAVAILABLE),
    CANCELLED(OwnerPickupRequestStatus.CANCELLED),
    EXPIRED(OwnerPickupRequestStatus.EXPIRED),
}

data class ProductListState(
    val products: List<OwnerProductModel> = emptyList(),
    val pickups: List<OwnerPickupModel> = emptyList(),
    val tab: ProductListTab = ProductListTab.PRODUCTS,
    val productFilter: OwnerProductFilter = OwnerProductFilter.ALL,
    val filter: ProductListFilter = ProductListFilter.ALL,
    val hasPickupError: Boolean = false,
    val mutationInProgress: Boolean = false,
    val filteredTotal: Long = 0,
    val filteredProductTotal: Long = 0,
    val totalProducts: Long? = null,
    val totalHolds: Long? = null,
    val cancellationCount: Int = 0,
) {
    val filteredPickups: List<OwnerPickupModel>
        get() = pickups.filter { filter.status == null || it.request.status == filter.status }
    val cancellationNeeded: List<OwnerPickupModel>
        get() = pickups.filter { it.needsCancellation }
}
