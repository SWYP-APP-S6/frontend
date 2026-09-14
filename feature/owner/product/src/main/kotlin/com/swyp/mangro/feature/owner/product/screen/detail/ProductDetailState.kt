package com.swyp.mangro.feature.owner.product.screen.detail

import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import java.io.Serializable

data class ProductDetailState(
    val product: OwnerProductModel? = null,
    val quantity: Int = 0,
    val showSaveConfirmation: Boolean = false,
    val showSaved: Boolean = false,
    val savedShortage: Int = 0,
) : Serializable {
    val canSave: Boolean
        get() = product != null && quantity >= 0 && quantity != product.remainingQuantity && !showSaved
}
