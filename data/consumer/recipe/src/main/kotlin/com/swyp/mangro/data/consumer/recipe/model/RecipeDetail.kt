package com.swyp.mangro.data.consumer.recipe.model

data class RecipeDetail(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val cookTimeMinutes: Int,
    val difficulty: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>,
    val nutrition: RecipeNutrition?,
)

data class RecipeIngredient(
    val seq: Int,
    val name: String,
    val amount: Double?,
    val unit: String,
    val rawText: String,
)

data class RecipeStep(
    val seq: Int,
    val content: String,
    val imageUrl: String,
)

data class RecipeNutrition(
    val basis: String,
    val calories: Double?,
    val carbsG: Double?,
    val proteinG: Double?,
    val fatG: Double?,
    val sodiumMg: Double?,
)
