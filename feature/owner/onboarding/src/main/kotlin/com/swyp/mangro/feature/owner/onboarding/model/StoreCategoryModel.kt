package com.swyp.mangro.feature.owner.onboarding.model

import java.io.Serializable
import kotlinx.collections.immutable.persistentListOf

data class StoreCategoryModel(val id: String, val label: String) : Serializable {
    companion object {
        val options = persistentListOf(
            StoreCategoryModel("VEGETABLE", "채소"),
            StoreCategoryModel("FRUIT", "과일"),
            StoreCategoryModel("MEAT", "육류"),
            StoreCategoryModel("SEAFOOD", "수산물"),
            StoreCategoryModel("DAIRY_EGG", "유제품/달걀"),
            StoreCategoryModel("BAKERY", "베이커리"),
            StoreCategoryModel("PREPARED_FOOD", "조리식품"),
            StoreCategoryModel("ETC", "기타"),
        )
    }
}
