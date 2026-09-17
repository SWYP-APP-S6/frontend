package com.swyp.mangro.feature.consumer.recipe.detail

sealed interface RecipeDetailUiEvent {
    data class NavigateToProductDetail(val productId: String) : RecipeDetailUiEvent
}
