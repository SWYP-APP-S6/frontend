package com.swyp.mangro.feature.owner.onboarding.model

import java.io.Serializable

data class StoreBasicInfoModel(
    val name: String = "",
    val category: StoreCategoryModel? = null,
    val address: StoreAddressModel? = null,
    val detailedAddress: String = "",
) : Serializable
