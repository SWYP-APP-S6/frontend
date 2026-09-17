package com.swyp.mangro.feature.owner.product

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.product.component.StockReconfirmationDialog
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StockReconfirmationDialogTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun rendersFigmaContentAndBlocksDuplicateAnswerWhileSaving() {
        val saving = mutableStateOf(false)
        var confirmed = 0
        var rejected = 0
        var deferred = 0
        compose.setContent {
            MangroTheme(typography = OwnerMangroTypography) {
                StockReconfirmationDialog(
                    "시금치 한 단",
                    4,
                    saving.value,
                    {
                        confirmed++
                        saving.value = true
                    },
                    { rejected++ },
                    { deferred++ },
                )
            }
        }
        compose.onNodeWithText("지금 남아있는 수량이").assertIsDisplayed()
        compose.onNodeWithText("4").assertIsDisplayed()
        compose.onNodeWithText("시금치 한 단").assertIsDisplayed()
        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir") ?: compose.activity.filesDir.path
        File(output, "stock-reconfirmation.png").outputStream().use {
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText("나중에하기").performClick()
        compose.onNodeWithText("아니요").performClick()
        compose.onNodeWithText("네, 맞아요").performClick()
        compose.onNodeWithText("네, 맞아요").assertIsNotEnabled()
        compose.onNodeWithText("아니요").assertIsNotEnabled()
        compose.onNodeWithText("나중에하기").assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals(1, confirmed)
            assertEquals(1, rejected)
            assertEquals(1, deferred)
        }
    }
}
