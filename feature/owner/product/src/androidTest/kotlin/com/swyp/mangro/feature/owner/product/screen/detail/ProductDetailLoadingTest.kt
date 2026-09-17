package com.swyp.mangro.feature.owner.product.screen.detail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProductDetailLoadingTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun missingProductIsNotAnErrorUntilRequestFails() {
        val state = mutableStateOf(ProductDetailState())
        val actions = mutableListOf<ProductDetailAction>()
        compose.setContent { MangroTheme { ProductDetailScreen(state.value, actions::add) } }
        val error = compose.activity.getString(R.string.pickup_error)
        val retry = compose.activity.getString(R.string.owner_management_retry)
        compose.onNodeWithText(error).assertDoesNotExist()
        compose.onNodeWithText(retry).assertDoesNotExist()
        compose.runOnIdle { state.value = ProductDetailState(hasError = true) }
        compose.onNodeWithText(error).assertIsDisplayed()
        compose.onNodeWithText(retry).performClick()
        compose.runOnIdle { assertEquals(listOf(ProductDetailAction.RetryClicked), actions) }
    }
}
