package com.swyp.mangro.feature.owner.product.screen.list

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestStatus
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
    val filter: ProductListFilter = ProductListFilter.ALL,
) {
    val filteredPickups: List<OwnerPickupModel>
        get() = pickups.filter { filter.status == null || it.request.status == filter.status }
    val filteredProducts: List<OwnerProductModel>
        get() = if (filter == ProductListFilter.ALL) {
            products
        } else {
            val ids = filteredPickups.map { it.productId }.toSet()
            products.filter { it.id in ids }
        }
    val cancellationNeeded: List<OwnerPickupModel>
        get() = pickups.filter { it.request.status == OwnerPickupRequestStatus.UNAVAILABLE }
}
