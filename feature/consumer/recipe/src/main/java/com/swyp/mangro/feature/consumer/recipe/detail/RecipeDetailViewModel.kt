package com.swyp.mangro.feature.consumer.recipe.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.model.recipe.RecipeDifficulty
import com.swyp.mangro.data.consumer.recipe.model.RecipeDetail
import com.swyp.mangro.data.consumer.recipe.model.RecipeIngredient
import com.swyp.mangro.data.consumer.recipe.model.RecipeNutrition
import com.swyp.mangro.data.consumer.recipe.repository.RecipeRepository
import com.swyp.mangro.feature.consumer.recipe.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RecipeRepository,
) : ViewModel() {
    private val recipeId: Long = checkNotNull(savedStateHandle["recipeId"])
    private val fromProductDetail: Boolean = savedStateHandle["fromProductDetail"] ?: false

    private val _uiState = MutableStateFlow(RecipeDetailUiState(showWishButton = fromProductDetail))
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    init {
        loadRecipeDetail()
    }

    private fun loadRecipeDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }
            repository.fetchRecipe(recipeId).collect { result ->
                result
                    .onSuccess { detail ->
                        _uiState.update {
                            it.copy(detail = detail.toUiModel(), isLoading = false)
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(isLoading = false, isError = true) }
                    }
            }
        }
    }
}

private fun RecipeDetail.toUiModel(): RecipeDetailInfo = RecipeDetailInfo(
    id = id,
    imageUrl = imageUrl,
    name = title,
    calorie = nutrition?.calories?.roundToInt() ?: 0,
    cookingMinutes = cookTimeMinutes,
    difficulty = difficulty.toRecipeDifficulty(),
    ingredients = ingredients
        .sortedBy { it.seq }
        .map { it.toLabel() }
        .filter { it.isNotBlank() }
        .toPersistentList(),
    nutritionBasisRes = when (nutrition?.basis) {
        "PER_SERVING" -> R.string.recipe_detail_nutrition_basis
        else -> null
    },
    nutrition = nutrition.toNutritionInfos(),
    steps = steps
        .sortedBy { it.seq }
        .mapIndexed { index, step ->
            RecipeStepInfo(
                stepNumber = index + 1,
                imageUrl = step.imageUrl,
                description = step.content,
            )
        }
        .toPersistentList(),
)

private fun String.toRecipeDifficulty(): RecipeDifficulty = when (uppercase()) {
    "EASY" -> RecipeDifficulty.LOW
    "HARD" -> RecipeDifficulty.HIGH
    else -> RecipeDifficulty.MEDIUM
}

private fun RecipeIngredient.toLabel(): String {
    if (rawText.isNotBlank()) return rawText
    val amountText = amount?.toDisplayNumber().orEmpty()
    return listOf(name, "$amountText$unit").filter { it.isNotBlank() }.joinToString(" ")
}

private fun RecipeNutrition?.toNutritionInfos(): ImmutableList<RecipeNutritionInfo> {
    if (this == null) return emptyList<RecipeNutritionInfo>().toPersistentList()
    return listOfNotNull(
        calories?.let { RecipeNutritionInfo(R.string.recipe_nutrition_calories, "${it.toDisplayNumber()}kcal") },
        carbsG?.let { RecipeNutritionInfo(R.string.recipe_nutrition_carbs, "${it.toDisplayNumber()}g") },
        proteinG?.let { RecipeNutritionInfo(R.string.recipe_nutrition_protein, "${it.toDisplayNumber()}g") },
        fatG?.let { RecipeNutritionInfo(R.string.recipe_nutrition_fat, "${it.toDisplayNumber()}g") },
        sodiumMg?.let { RecipeNutritionInfo(R.string.recipe_nutrition_sodium, "${it.toDisplayNumber()}mg") },
    ).toPersistentList()
}

private fun Double.toDisplayNumber(): String = if (this % 1.0 == 0.0) toLong().toString() else "%.1f".format(this)
