package com.swyp.mangro.feature.owner.setting.screen

import com.swyp.mangro.feature.owner.setting.model.SettingsMenu

sealed interface OwnerSettingAction {
    data object LogoutConfirmed : OwnerSettingAction
    data object WithdrawConfirmed : OwnerSettingAction
    data class SettingsMenuClicked(val menu: SettingsMenu) : OwnerSettingAction
}
