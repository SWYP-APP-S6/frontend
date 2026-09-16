package com.swyp.mangro.feature.owner.product.screen.editor.pickup

import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.util.OwnerProductLimits
import java.io.Serializable

data class ProductPickupInfoState(
    val product: OwnerProductModel? = null,
    val isLoading: Boolean = true,
    val storeCategory: String? = null,
    val storeClosingTime: String = "",
    val storeOpeningTime: String = "",
    val pickupTimeOptions: List<String> = emptyList(),
    val pickupTime: String? = null,
    val tags: List<String> = emptyList(),
    val tagInput: String = "",
    val showPreview: Boolean = false,
) : Serializable {
    val canAddTag: Boolean get() = tagInput.isNotBlank() && tags.size < OwnerProductLimits.TAG_COUNT && tagInput.trim() !in tags
    val canPreview: Boolean get() = product != null && (pickupTime ?: storeClosingTime) in pickupTimeOptions && (tagInput.isBlank() || canAddTag)
    val draft: OwnerProductModel? get() = product?.copy(pickupEndTime = pickupTime ?: storeClosingTime, tags = tags)
}
