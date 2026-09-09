package com.swyp.mangro

import android.content.Context
import com.swyp.mangro.feature.owner.home.OwnerHomeSamples
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeUiState
import kotlinx.collections.immutable.toPersistentList

internal fun initialOwnerHomeState(context: Context): OwnerHomeUiState = OwnerHomeSamples.operating().let { sample ->
    sample.copy(
        products = sample.products.map { product ->
            product.copy(imageUrl = product.imageUrl.replace("com.swyp.mangro.feature.owner.home", context.packageName))
        }.toPersistentList(),
    )
}
