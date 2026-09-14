package com.swyp.mangro.feature.owner.onboarding.screen.operating

import com.swyp.mangro.feature.owner.onboarding.model.StoreBasicInfoModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreRegistrationModel
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

data class OwnerOperatingInfoState(
    val basicInfo: StoreBasicInfoModel = StoreBasicInfoModel(),
    val phoneNumber: String = "",
    val openingMinutes: Int? = null,
    val closingMinutes: Int? = null,
    val businessDays: PersistentSet<Int> = persistentSetOf(),
    val isLoading: Boolean = false,
    val dialog: OwnerOperatingInfoDialog? = null,
    val isCompletionHandled: Boolean = false,
) {
    val registration: StoreRegistrationModel
        get() = StoreRegistrationModel(basicInfo.name.trim(), basicInfo.category, basicInfo.address, basicInfo.detailedAddress.trim(), phoneNumber.filter(Char::isDigit), openingMinutes, closingMinutes, businessDays.toSet())
    val isSubmitEnabled: Boolean
        get() = registration.isValid && !isLoading && dialog != OwnerOperatingInfoDialog.Submitted && !isCompletionHandled
}

enum class OwnerOperatingInfoDialog { Error, Submitted }
