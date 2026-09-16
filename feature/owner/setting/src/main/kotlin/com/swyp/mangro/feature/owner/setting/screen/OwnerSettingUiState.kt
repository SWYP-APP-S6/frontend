package com.swyp.mangro.feature.owner.setting.screen

data class OwnerSettingUiState(
    val storeName: String = "",
    val storePhone: String = "",
    val isLoading: Boolean = false,
    val hasStoreError: Boolean = false,
    val isLoggingOut: Boolean = false,
    val hasLogoutError: Boolean = false,
)
