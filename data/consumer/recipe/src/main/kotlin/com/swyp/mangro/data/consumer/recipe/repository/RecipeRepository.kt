package com.swyp.mangro.data.consumer.recipe.repository

import com.swyp.mangro.data.consumer.recipe.model.RecipeDetail
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun fetchRecipe(recipeId: Long): Flow<Result<RecipeDetail>>
}
