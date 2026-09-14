package com.swyp.mangro

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingAction
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingScreen
import com.swyp.mangro.feature.owner.setting.screen.OwnerSettingUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OwnerSettingLayoutTest {
    @get:Rule
    val compose = createAndroidComposeRule<ProductTestActivity>()

    @Test
    fun missingStoreInformationIsShownWithoutSampleValues() {
        compose.setContent {
            MangroTheme(typography = OwnerMangroTypography) {
                OwnerSettingScreen(uiState = OwnerSettingUiState(), onAction = {})
            }
        }
        compose.onAllNodesWithText("미등록")[0].assertIsDisplayed()
        compose.onAllNodesWithText("미등록")[1].assertIsDisplayed()
        compose.onNodeWithText("개인정보 처리방침").assertIsDisplayed()
    }

    @Test
    fun longStoreNameAndLargeFontKeepPoliciesReachable() {
        val storeName = "우리 동네 신선한 과일과 채소를 판매하는 청과마을"
        val actions = mutableListOf<OwnerSettingAction>()
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.5f)) {
                MangroTheme(typography = OwnerMangroTypography) {
                    OwnerSettingScreen(
                        uiState = OwnerSettingUiState(storeName = storeName, storePhone = "010-1234-5678"),
                        onAction = { actions += it },
                    )
                }
            }
        }
        compose.onNodeWithText(storeName).assertIsDisplayed()
        compose.onNodeWithText("개인정보 처리방침").performScrollTo().assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(OwnerSettingAction.PolicyClicked(OwnerPolicy.PRIVACY_POLICY), actions.last())
        }
    }
}
