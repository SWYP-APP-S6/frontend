package com.swyp.mangro.feature.owner.product.screen.detail

import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import java.io.Serializable

data class ProductDetailState(
    val product: OwnerProductModel? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasError: Boolean = false,
    val quantity: Int = 0,
    val showSaveConfirmation: Boolean = false,
    val showSaved: Boolean = false,
    val savedShortage: Int = 0,
) : Serializable {
    val canSave: Boolean
        get() = product != null && product.stockEditable && !isLoading && !isSaving && !hasError && quantity in product.minAdjustableQuantity..9999 && quantity != product.remainingQuantity && !showSaved
}
