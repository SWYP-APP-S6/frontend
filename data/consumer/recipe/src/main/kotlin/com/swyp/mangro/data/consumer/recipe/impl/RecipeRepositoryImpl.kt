package com.swyp.mangro.data.consumer.recipe.impl

import com.swyp.mangro.data.consumer.recipe.model.RecipeDetail
import com.swyp.mangro.data.consumer.recipe.model.RecipeIngredient
import com.swyp.mangro.data.consumer.recipe.model.RecipeNutrition
import com.swyp.mangro.data.consumer.recipe.model.RecipeStep
import com.swyp.mangro.data.consumer.recipe.repository.RecipeRepository
import com.swyp.mangro.remote.consumer.model.RecipeDetailResponse
import com.swyp.mangro.remote.consumer.service.RecipeService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

internal class RecipeRepositoryImpl @Inject constructor(
    private val recipeService: RecipeService,
) : RecipeRepository {

    override fun fetchRecipe(recipeId: Long): Flow<Result<RecipeDetail>> = request {
        require(recipeId > 0)
        val response = recipeService.fetchRecipe(recipeId)
        if (!response.isSuccessful) {
            response.errorBody()?.close()
            throw HttpException(response)
        }
        requireNotNull(response.body()).toDomain()
    }
}

private fun RecipeDetailResponse.toDomain(): RecipeDetail = RecipeDetail(
    id = id ?: 0L,
    title = title.orEmpty(),
    imageUrl = imageUrl.orEmpty(),
    cookTimeMinutes = cookTimeMinutes ?: 0,
    difficulty = difficulty?.value.orEmpty(),
    ingredients = ingredients.orEmpty().map { ingredient ->
        RecipeIngredient(
            seq = ingredient.seq ?: 0,
            name = ingredient.ingredientName.orEmpty(),
            amount = ingredient.amount,
            unit = ingredient.unit.orEmpty(),
            rawText = ingredient.rawText.orEmpty(),
        )
    },
    steps = steps.orEmpty().map { step ->
        RecipeStep(
            seq = step.seq ?: 0,
            content = step.content.orEmpty(),
            imageUrl = step.imageUrl.orEmpty(),
        )
    },
    nutrition = nutrition?.let {
        RecipeNutrition(
            basis = it.basis.orEmpty(),
            calories = it.calories,
            carbsG = it.carbsG,
            proteinG = it.proteinG,
            fatG = it.fatG,
            sodiumMg = it.sodiumMg,
        )
    },
)

private fun <T> request(block: suspend () -> T): Flow<Result<T>> = flow {
    val result = try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
    emit(result)
}.flowOn(Dispatchers.IO)
