package com.swyp.mangro.feature.owner.product.screen.editor.basic

import com.swyp.mangro.feature.owner.product.model.ProductDraftModel

sealed interface ProductBasicInfoEvent {
    data class Next(val draft: ProductDraftModel) : ProductBasicInfoEvent
    data object Exit : ProductBasicInfoEvent
}
