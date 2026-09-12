package com.swyp.mangro.feature.owner.onboarding.screen.basic

import com.swyp.mangro.feature.owner.onboarding.model.StoreAddressModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreCategoryModel

sealed interface OwnerBasicInfoAction {
    data class NameChanged(val value: String) : OwnerBasicInfoAction
    data class DetailedAddressChanged(val value: String) : OwnerBasicInfoAction
    data class CategorySelected(val value: StoreCategoryModel) : OwnerBasicInfoAction
    data class AddressSelected(val value: StoreAddressModel) : OwnerBasicInfoAction
    data class CategoriesReceived(val values: List<StoreCategoryModel>) : OwnerBasicInfoAction
    data object NextClicked : OwnerBasicInfoAction
    data object AddressSearchClicked : OwnerBasicInfoAction
    data object NavigationBackClicked : OwnerBasicInfoAction
}
