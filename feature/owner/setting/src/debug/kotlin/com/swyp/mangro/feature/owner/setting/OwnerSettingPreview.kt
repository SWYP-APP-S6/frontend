package com.swyp.mangro.feature.owner.setting

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingScreen
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingUiState

@Preview(showBackground = true)
@Composable
private fun OwnerSettingPreview() {
    MangroTheme(typography = OwnerMangroTypography) {
        OwnerSettingScreen(
            uiState = OwnerSettingUiState(storeName = "청과마을", storePhone = "02-1234-5678"),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
private fun OwnerSettingLongNamePreview() {
    MangroTheme(typography = OwnerMangroTypography) {
        OwnerSettingScreen(
            uiState = OwnerSettingUiState(storeName = "우리 동네 신선한 과일과 채소를 판매하는 청과마을", storePhone = "010-1234-5678"),
            onAction = {},
        )
    }
}
