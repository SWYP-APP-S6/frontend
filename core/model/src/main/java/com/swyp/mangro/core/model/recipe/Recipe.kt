package com.swyp.mangro.core.model.recipe

import kotlinx.collections.immutable.ImmutableList

data class Recipe(
    val id: Long,
    val difficulty: RecipeDifficulty,
    val name: String,
    val ingredients: ImmutableList<String>,
)
