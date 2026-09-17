package com.swyp.mangro.feature.owner.product.screen.editor.pickup

import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import java.io.Serializable

data class ProductPickupInfoState(
    val product: OwnerProductModel? = null,
    val isLoading: Boolean = true,
    val storeCategory: String? = null,
    val storeClosingTime: String = "",
    val storeOpeningTime: String = "",
    val pickupTimeOptions: List<String> = emptyList(),
    val pickupTime: String? = null,
    val showPreview: Boolean = false,
) : Serializable {
    val canPreview: Boolean get() = product != null && (pickupTime ?: storeClosingTime) in pickupTimeOptions
    val draft: OwnerProductModel? get() = product?.copy(pickupEndTime = pickupTime ?: storeClosingTime)
}
