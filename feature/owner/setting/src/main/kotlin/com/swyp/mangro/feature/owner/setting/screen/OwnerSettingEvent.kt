package com.swyp.mangro.feature.owner.setting.screen

import com.swyp.mangro.feature.owner.setting.model.SettingsMenu

sealed interface OwnerSettingEvent {
    data object NavigateToLogin : OwnerSettingEvent
    data object NavigateToHome : OwnerSettingEvent
    data object NavigateToProducts : OwnerSettingEvent
    data class NavigateToPolicy(val policy: SettingsMenu) : OwnerSettingEvent

    data object ShowLogoutConfirmDialog : OwnerSettingEvent
    data object ShowLogoutErrorDialog : OwnerSettingEvent
    data object ShowWithdrawConfirmDialog : OwnerSettingEvent

    data object ShowWithdrawErrorDialog : OwnerSettingEvent
}
