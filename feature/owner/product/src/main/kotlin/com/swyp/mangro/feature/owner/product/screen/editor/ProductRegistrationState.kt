package com.swyp.mangro.feature.owner.product.screen.editor

import com.swyp.mangro.data.owner.store.model.OwnerStore

data class ProductRegistrationState(
    val store: OwnerStore? = null,
    val isChecking: Boolean = true,
    val checkFailed: Boolean = false,
)
