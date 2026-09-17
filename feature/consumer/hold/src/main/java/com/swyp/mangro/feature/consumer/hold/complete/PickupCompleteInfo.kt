package com.swyp.mangro.feature.consumer.hold.complete

import com.swyp.mangro.core.designsystem.component.card.purchase.PurchaseInfo
import com.swyp.mangro.core.model.recipe.Recipe
import kotlinx.collections.immutable.ImmutableList

data class PickupCompleteInfo(
    val purchaseInfo: PurchaseInfo,
    val recommendedRecipes: ImmutableList<Recipe>,
)
