package com.swyp.mangro.feature.consumer.recipe.detail

import androidx.annotation.StringRes
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
    @StringRes val nutritionBasisRes: Int?,
    val nutrition: ImmutableList<RecipeNutritionInfo>,
    val steps: ImmutableList<RecipeStepInfo>,
)

data class RecipeNutritionInfo(
    @StringRes val nameRes: Int,
    val value: String,
)

data class RecipeStepInfo(
    val stepNumber: Int,
    val imageUrl: String,
    val description: String,
)
