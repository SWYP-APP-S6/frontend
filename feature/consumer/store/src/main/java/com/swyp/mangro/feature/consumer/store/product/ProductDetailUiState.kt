package com.swyp.mangro.feature.consumer.store.product

import com.swyp.mangro.core.model.product.Product
import com.swyp.mangro.core.model.recipe.Recipe
import com.swyp.mangro.core.model.store.StoreInfo
import com.swyp.mangro.feature.consumer.store.wish.WishUiState
import kotlinx.collections.immutable.ImmutableList

data class ProductInfo(
    val product: Product,
    val images: ImmutableList<String>,
    val tags: ImmutableList<String>,
    val store: StoreInfo,
    val recipes: ImmutableList<Recipe>,
)

data class ProductDetailUiState(
    val productInfo: ProductInfo? = null,
    val wishState: WishUiState = WishUiState(),
    val isWishBottomSheetVisible: Boolean = false,
)
