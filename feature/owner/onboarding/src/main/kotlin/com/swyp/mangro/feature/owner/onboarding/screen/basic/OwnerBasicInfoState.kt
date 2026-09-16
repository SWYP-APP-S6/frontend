package com.swyp.mangro.feature.owner.onboarding.screen.basic

import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreCategoryModel

data class OwnerBasicInfoState(
    val name: String = "",
    val detailedAddress: String = "",
    val category: StoreCategoryModel? = null,
    val address: StoreAddressModel? = null,
) {
    val isNextEnabled: Boolean
        get() = name.isNotBlank() &&
            name.trim().length <= 100 &&
            detailedAddress.trim().length <= 255 &&
            category != null &&
            address?.let { it.postalCode.matches(Regex("[0-9]{5}")) && it.address.isNotBlank() && it.address.length <= 255 } == true
}
