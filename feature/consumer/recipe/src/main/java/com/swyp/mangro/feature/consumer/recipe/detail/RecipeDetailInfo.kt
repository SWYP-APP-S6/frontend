package com.swyp.mangro.feature.consumer.recipe.detail

import com.swyp.mangro.core.model.recipe.RecipeDifficulty
import kotlinx.collections.immutable.ImmutableList

data class RecipeDetailInfo(
    val id: Long,
    val imageUrl: String,
    val name: String,
    val calorie: Int,
    val cookingMinutes: Int,
    val difficulty: RecipeDifficulty,
    val ingredients: ImmutableList<String>,
    val nutritionLabels: ImmutableList<String>,
    val steps: ImmutableList<RecipeStepInfo>,
    val relatedProductId: String,
)

data class RecipeStepInfo(
    val stepNumber: Int,
    val imageUrl: String,
    val description: String,
)
