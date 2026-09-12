package com.swyp.mangro.feature.owner.onboarding.screen.basic

import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreCategoryModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class OwnerBasicInfoState(
    val name: String = "",
    val detailedAddress: String = "",
    val category: StoreCategoryModel? = null,
    val address: StoreAddressModel? = null,
    val categories: PersistentList<StoreCategoryModel> = persistentListOf(),
) {
    val isNextEnabled: Boolean
        get() = name.isNotBlank() &&
            category != null &&
            address?.let { it.postalCode.matches(Regex("[0-9]{5}")) && it.address.isNotBlank() } == true
}
