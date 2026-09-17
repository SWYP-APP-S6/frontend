package com.swyp.mangro.feature.consumer.recipe.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.swyp.mangro.feature.consumer.recipe.detail.RecipeDetailRoute
import kotlinx.serialization.Serializable

@Serializable
data class RecipeDetailDestination(val recipeId: Long)

fun NavGraphBuilder.recipeDetailScreen(
    navController: NavController,
    onNavigateToProductDetail: (String) -> Unit,
) {
    composable<RecipeDetailDestination> {
        RecipeDetailRoute(
            onBackClick = { navController.popBackStack() },
            onNavigateToProductDetail = onNavigateToProductDetail,
        )
    }
}

fun NavController.navigateToRecipeDetail(recipeId: Long) {
    navigate(RecipeDetailDestination(recipeId = recipeId))
}
