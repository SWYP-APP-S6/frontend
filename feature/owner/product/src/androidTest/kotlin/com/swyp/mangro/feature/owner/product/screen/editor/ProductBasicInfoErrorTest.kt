package com.swyp.mangro.feature.owner.product.screen.editor

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.product.screen.editor.basic.ProductBasicInfoAction
import com.swyp.mangro.feature.owner.product.screen.editor.basic.ProductBasicInfoScreen
import com.swyp.mangro.feature.owner.product.screen.editor.basic.ProductBasicInfoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class ProductBasicInfoErrorTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun photoPermissionErrorCanBeDismissedWithoutLosingDraft() {
        verifyError(
            name = "복숭아",
            action = ProductBasicInfoAction.PhotoPermissionFailed,
            message = "사진을 불러오지 못했어요. 다른 사진을 선택해주세요.",
        )
    }

    @Test
    fun invalidNameErrorCanBeDismissedAndInputRemainsInvalid() {
        val name = "가".repeat(26)
        val viewModel = verifyError(
            name = name,
            action = ProductBasicInfoAction.NameChanged(name),
            message = "품목명은 공백을 제외한 내용이 있어야 하며 최대 25자예요.",
        )
        assertFalse(viewModel.uiState.value.canContinue)
    }

    private fun verifyError(name: String, action: ProductBasicInfoAction, message: String): ProductBasicInfoViewModel {
        val viewModel = ProductBasicInfoViewModel(SavedStateHandle())
        viewModel.initialize()
        viewModel.handleAction(ProductBasicInfoAction.NameChanged(name))
        viewModel.handleAction(ProductBasicInfoAction.PhotosSelected(listOf("file://photo")))
        viewModel.handleAction(action)
        compose.setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            MangroTheme(typography = OwnerMangroTypography) {
                ProductBasicInfoScreen(state, viewModel::handleAction)
            }
        }
        compose.onNode(isDialog()).assertIsDisplayed()
        compose.onNodeWithText(message).assertIsDisplayed()
        compose.onNodeWithText("확인").performClick()
        compose.onNode(isDialog()).assertDoesNotExist()
        compose.runOnIdle {
            assertNull(viewModel.uiState.value.error)
            assertEquals(name, viewModel.uiState.value.name)
            assertEquals(listOf("file://photo"), viewModel.uiState.value.photos)
        }
        return viewModel
    }
}
