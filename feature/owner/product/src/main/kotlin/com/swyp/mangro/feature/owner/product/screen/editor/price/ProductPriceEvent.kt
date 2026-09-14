package com.swyp.mangro.feature.owner.product.screen.editor.price

import com.swyp.mangro.feature.owner.product.model.ProductDraftModel

sealed interface ProductPriceEvent {
    data class Next(val draft: ProductDraftModel) : ProductPriceEvent
    data object Back : ProductPriceEvent
}
