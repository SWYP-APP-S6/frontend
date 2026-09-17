package com.swyp.mangro.core.model.product

enum class ProductCategory {
    GRAINS,
    VEGETABLES,
    MEAT,
    SEAFOOD,
    NUTS,
    FRUIT,
    DAIRY_EGG,
    BAKERY,
    PREPARED_FOOD,
    ETC,
    ;

    companion object {
        fun fromStoreCategory(category: String?): ProductCategory = when (category) {
            "VEGETABLE" -> VEGETABLES
            "FRUIT" -> FRUIT
            "MEAT" -> MEAT
            "SEAFOOD" -> SEAFOOD
            "DAIRY_EGG" -> DAIRY_EGG
            "BAKERY" -> BAKERY
            "PREPARED_FOOD" -> PREPARED_FOOD
            else -> ETC
        }
    }
}
