package com.swyp.mangro.feature.consumer.recipe.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.consumer.recipe.detail.RecipeDetailRoute
import kotlinx.serialization.Serializable

@Serializable
data class RecipeDetailDestination(
    val recipeId: Long,
    val fromProductDetail: Boolean = false,
)

fun NavGraphBuilder.recipeDetailScreen(
    navController: NavController,
) {
    composable<RecipeDetailDestination> {
        RecipeDetailRoute(
            onBackClick = { navController.popBackStack() },
            onWishClick = { navController.popBackStack() },
        )
    }
}

fun NavController.navigateToRecipeDetail(recipeId: Long, fromProductDetail: Boolean = false) {
    navigate(RecipeDetailDestination(recipeId = recipeId, fromProductDetail = fromProductDetail))
}
