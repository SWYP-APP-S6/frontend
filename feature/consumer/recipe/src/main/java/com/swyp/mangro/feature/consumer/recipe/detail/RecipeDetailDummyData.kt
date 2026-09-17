package com.swyp.mangro.feature.consumer.recipe.detail

import com.swyp.mangro.core.model.recipe.RecipeDifficulty
import kotlinx.collections.immutable.persistentListOf

val dummyRecipeDetailInfo = RecipeDetailInfo(
    id = 1L,
    imageUrl = "",
    name = "복숭아 샐러드",
    calorie = 167,
    cookingMinutes = 20,
    difficulty = RecipeDifficulty.LOW,
    ingredients = persistentListOf("복숭아 300g", "복숭아 300g", "복숭아 300g", "복숭아 300g"),
    nutritionLabels = persistentListOf("열량", "탄수화물", "단백질", "지방", "나트륨"),
    steps = persistentListOf(
        RecipeStepInfo(stepNumber = 1, imageUrl = "", description = "복숭아를 한 입 크기로 썬다"),
        RecipeStepInfo(stepNumber = 2, imageUrl = "", description = "요거트를 볼에 담는다"),
    ),
    relatedProductId = "1",
)

val dummyRecipeDetailUiState = RecipeDetailUiState(
    detail = dummyRecipeDetailInfo,
)
