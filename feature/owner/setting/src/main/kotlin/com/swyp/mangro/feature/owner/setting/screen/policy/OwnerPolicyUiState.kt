package com.swyp.mangro.feature.owner.setting.screen.policy

import com.swyp.mangro.feature.owner.setting.model.SettingsMenu

data class OwnerPolicyUiState(
    val policy: SettingsMenu = SettingsMenu.TERMS_OF_SERVICE,
    val content: String = "",
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)
