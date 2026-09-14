package com.swyp.mangro.feature.owner.setting.screen

import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy

sealed interface OwnerSettingEvent {
    data object NavigateToHome : OwnerSettingEvent
    data object NavigateToProducts : OwnerSettingEvent
    data class NavigateToPolicy(val policy: OwnerPolicy) : OwnerSettingEvent
}
