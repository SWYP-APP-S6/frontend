package com.swyp.mangro.feature.consumer.recipe.detail

import com.swyp.mangro.core.model.recipe.RecipeDifficulty
import com.swyp.mangro.feature.consumer.recipe.R
import kotlinx.collections.immutable.persistentListOf

val dummyRecipeDetailInfo = RecipeDetailInfo(
    id = 1L,
    imageUrl = "",
    name = "복숭아 샐러드",
    calorie = 167,
    cookingMinutes = 20,
    difficulty = RecipeDifficulty.LOW,
    ingredients = persistentListOf("복숭아 300g", "요거트 100g", "꿀 1큰술", "견과류 20g"),
    nutritionBasisRes = R.string.recipe_detail_nutrition_basis,
    nutrition = persistentListOf(
        RecipeNutritionInfo(R.string.recipe_nutrition_calories, "167kcal"),
        RecipeNutritionInfo(R.string.recipe_nutrition_carbs, "32g"),
        RecipeNutritionInfo(R.string.recipe_nutrition_protein, "5g"),
        RecipeNutritionInfo(R.string.recipe_nutrition_fat, "3g"),
        RecipeNutritionInfo(R.string.recipe_nutrition_sodium, "40mg"),
    ),
    steps = persistentListOf(
        RecipeStepInfo(stepNumber = 1, imageUrl = "", description = "복숭아를 한 입 크기로 썬다"),
        RecipeStepInfo(stepNumber = 2, imageUrl = "", description = "요거트를 볼에 담는다"),
    ),
)

val dummyRecipeDetailUiState = RecipeDetailUiState(
    detail = dummyRecipeDetailInfo,
)
