package com.swyp.mangro.feature.owner.onboarding.screen.basic

import com.swyp.mangro.feature.owner.onboarding.model.StoreBasicInfoModel

sealed interface OwnerBasicInfoEvent {
    data class NavigateToOperatingInfo(val basicInfo: StoreBasicInfoModel) : OwnerBasicInfoEvent
    data object NavigateToAddressSearch : OwnerBasicInfoEvent
    data object NavigateBack : OwnerBasicInfoEvent
}
