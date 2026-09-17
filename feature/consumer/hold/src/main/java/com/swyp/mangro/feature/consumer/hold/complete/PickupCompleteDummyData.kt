package com.swyp.mangro.feature.consumer.hold.complete

import com.swyp.mangro.core.designsystem.component.card.purchase.PurchaseInfo
import com.swyp.mangro.core.model.recipe.Recipe
import com.swyp.mangro.core.model.recipe.RecipeDifficulty
import kotlinx.collections.immutable.persistentListOf

val dummyPickupCompleteInfo = PickupCompleteInfo(
    purchaseInfo = PurchaseInfo(
        date = "2026.09.04",
        storeName = "청과마을",
        productName = "복숭아 4입",
        quantity = 1,
        price = 4_000,
    ),
    recommendedRecipes = persistentListOf(
        Recipe(
            id = 1L,
            name = "복숭아 샐러드",
            difficulty = RecipeDifficulty.LOW,
            ingredients = persistentListOf("복숭아", "요거트", "견과류"),
        ),
        Recipe(
            id = 2L,
            name = "복숭아 얼그레이 샐러드",
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = persistentListOf("복숭아", "요거트", "견과류", "얼그레이"),
        ),
        Recipe(
            id = 3L,
            name = "복숭아 마스카포네치즈 카나페",
            difficulty = RecipeDifficulty.HIGH,
            ingredients = persistentListOf("복숭아", "요거트", "견과류", "마스카포네치즈"),
        ),
    ),
)

val dummyPickupCompleteUiState = PickupCompleteUiState(
    info = dummyPickupCompleteInfo,
)
