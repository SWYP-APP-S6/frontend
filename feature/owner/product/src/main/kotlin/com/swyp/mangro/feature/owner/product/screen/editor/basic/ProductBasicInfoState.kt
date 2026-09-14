package com.swyp.mangro.feature.owner.product.screen.editor.basic

import com.swyp.mangro.feature.owner.product.util.OwnerProductLimits
import com.swyp.mangro.feature.owner.product.util.isValidProductName
import java.io.Serializable

data class ProductBasicInfoState(
    val isLoading: Boolean = true,
    val name: String = "",
    val photos: List<String> = emptyList(),
    val error: ProductBasicInfoError? = null,
    val showDiscard: Boolean = false,
) : Serializable {
    val validName: Boolean get() = isValidProductName(name)
    val canContinue: Boolean get() = photos.size in 1..OwnerProductLimits.PHOTO_COUNT && validName
}

enum class ProductBasicInfoError {
    PHOTO_PERMISSION,
    INVALID_NAME,
}
