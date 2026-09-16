package com.swyp.mangro.feature.consumer.recipe.detail

sealed interface RecipeDetailUiAction {
    data object OnWishClick : RecipeDetailUiAction
}
