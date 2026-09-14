package com.swyp.mangro.feature.owner.product.screen.editor

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.editor.basic.ProductBasicInfoScreen
import com.swyp.mangro.feature.owner.product.screen.editor.basic.ProductBasicInfoState
import com.swyp.mangro.feature.owner.product.screen.editor.pickup.ProductPickupInfoAction
import com.swyp.mangro.feature.owner.product.screen.editor.pickup.ProductPickupInfoScreen
import com.swyp.mangro.feature.owner.product.screen.editor.pickup.ProductPickupInfoState
import com.swyp.mangro.feature.owner.product.screen.editor.price.ProductPriceScreen
import com.swyp.mangro.feature.owner.product.screen.editor.price.ProductPriceState
import java.io.File
import org.junit.Rule
import org.junit.Test

class ProductEditorDesignTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun show(content: @Composable () -> Unit) {
        compose.setContent { MangroTheme(typography = OwnerMangroTypography, content = content) }
    }

    @Test fun basicMatchesFigma1155_11416() {
        show { ProductBasicInfoScreen(ProductBasicInfoState(), {}) }
        compose.onNodeWithText("오늘은 어떤 상품을").assertIsDisplayed()
        compose.onNodeWithText("판매하실 건가요?").assertIsDisplayed()
        compose.onNodeWithText("최대 5장까지 올릴 수 있어요.").assertIsDisplayed()
        compose.onNodeWithText("사진 올리기").assertIsDisplayed().assertIsEnabled()
        compose.onNodeWithText("예시) 복숭아 4입").assertIsDisplayed()
        compose.onNodeWithText("다음").assertIsNotEnabled()
        capture("editor-basic")
    }

    @Test fun priceMatchesFigma1155_13956() {
        show { ProductPriceScreen(ProductPriceState(), {}) }
        compose.onNodeWithText("어떤 가격에,").assertIsDisplayed()
        compose.onNodeWithText("얼마나 판매할까요?").assertIsDisplayed()
        compose.onNodeWithText("최종 할인율").assertIsDisplayed()
        compose.onNodeWithText("0%").assertIsDisplayed()
        compose.onNodeWithText("0원이 저렴해져요").assertIsDisplayed()
        compose.onNodeWithText("다음").assertIsNotEnabled()
        capture("editor-price")
    }

    @Test fun pickupMatchesFigma1155_14029AndTagsAreRemovable() {
        val product = OwnerProductModel("peach", "복숭아", listOf("fixture"), 10000, 4000, 1, 1, pickupEndTime = "20:00")
        show {
            var state by remember { mutableStateOf(ProductPickupInfoState(product = product, storeClosingTime = "20:00", pickupTimeOptions = listOf("19:00", "20:00"), tags = listOf("복숭아", "청과"))) }
            ProductPickupInfoScreen(state) { action ->
                if (action is ProductPickupInfoAction.TagRemoveClicked) state = state.copy(tags = state.tags - action.value)
                if (action is ProductPickupInfoAction.PickupTimeChanged) state = state.copy(pickupTime = action.value)
            }
        }
        compose.onNodeWithText("판매에 필요한 정보를").assertIsDisplayed()
        compose.onNodeWithText("오늘 오후 20:00").assertIsDisplayed()
        compose.onNodeWithText("태그 추가").assertDoesNotExist()
        compose.onNodeWithText("복숭아").assertIsDisplayed()
        compose.onNodeWithText("청과").assertIsDisplayed()
        compose.onNodeWithText("등록하기").assertIsEnabled()
        capture("editor-pickup")
        compose.onNodeWithText("오늘 오후 20:00").performClick()
        compose.onNodeWithText("19:00").assertIsDisplayed()
        compose.onNodeWithText("18:00").assertDoesNotExist()
        compose.onNodeWithText("19:00").performClick()
        compose.onNodeWithText("19:00").assertIsDisplayed()
        compose.onNodeWithContentDescription("복숭아 태그 삭제").performClick()
        compose.onNodeWithText("복숭아").assertDoesNotExist()
        compose.onNodeWithText("청과").assertIsDisplayed()
    }

    private fun capture(name: String) {
        val file = File(compose.activity.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
