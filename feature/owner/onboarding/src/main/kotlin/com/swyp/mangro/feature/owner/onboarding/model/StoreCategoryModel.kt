package com.swyp.mangro.feature.owner.onboarding.model

import java.io.Serializable
import kotlinx.collections.immutable.persistentListOf

data class StoreCategoryModel(val id: String, val label: String) : Serializable {
    companion object {
        val options = persistentListOf(
            StoreCategoryModel("debug-0", "곡류"),
            StoreCategoryModel("debug-1", "과채류"),
            StoreCategoryModel("debug-2", "육류"),
            StoreCategoryModel("debug-3", "어류"),
            StoreCategoryModel("debug-4", "견과류"),
            StoreCategoryModel("debug-5", "기타"),
        )
    }
}
