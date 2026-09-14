package com.swyp.mangro.feature.owner.setting.screen

import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy

sealed interface OwnerSettingAction {
    data class MenuClicked(val menu: OwnerMenu) : OwnerSettingAction
    data class PolicyClicked(val policy: OwnerPolicy) : OwnerSettingAction
    data object NavigationBackClicked : OwnerSettingAction
}
