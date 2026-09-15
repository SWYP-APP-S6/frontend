package com.swyp.mangro.feature.consumer.store.product

import com.swyp.mangro.core.model.product.Product
import com.swyp.mangro.core.model.product.ProductCategory
import com.swyp.mangro.core.model.recipe.RecipeDifficulty
import com.swyp.mangro.feature.consumer.store.wish.WishUiState
import kotlinx.collections.immutable.persistentListOf

val dummyProductInfo = ProductInfo(
    product = Product(
        id = "1",
        imageUrl = "",
        discountRate = 50,
        name = "복숭아 4입",
        price = 4_000,
        originalPrice = 10_000,
        category = ProductCategory.VEGETABLES,
        remainingCount = 3,
    ),
    images = persistentListOf("", "", "", "", ""),
    tags = persistentListOf("복숭아", "청과", "떡목", "쌀목", "식자재"),
    store = StoreInfo(
        id = 1L,
        name = "청과마을",
        address = "서울 마포구 망원로 12",
        distanceMeters = 450,
        travelInfo = "도보 7분",
        closingTime = "오늘 20:00까지",
    ),
    recipes = persistentListOf(
        Recipe(id = 1L, difficulty = RecipeDifficulty.LOW, name = "복숭아 샐러드", ingredients = persistentListOf("복숭아", "요거트", "견과류")),
        Recipe(id = 2L, difficulty = RecipeDifficulty.MEDIUM, name = "복숭아 얼그레이 샐러드", ingredients = persistentListOf("복숭아", "요거트", "견과류", "얼그레이")),
    ),
)

val dummyProductDetailUiState = ProductDetailUiState(
    productInfo = dummyProductInfo,
    wishState = WishUiState(quantity = 1),
    isWishBottomSheetVisible = false,
)

val dummyProductDetailUiStateWishSheetOpen = dummyProductDetailUiState.copy(
    isWishBottomSheetVisible = true,
)
