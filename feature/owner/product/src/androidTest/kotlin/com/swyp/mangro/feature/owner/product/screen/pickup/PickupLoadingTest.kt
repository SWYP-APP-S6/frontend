package com.swyp.mangro.feature.owner.product.screen.pickup

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.model.Pickup
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationAction
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationScreen
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.PickupCancellationState
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.PickupDetailAction
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.PickupDetailScreen
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.PickupDetailState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PickupLoadingTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun detailLoadingAndFailureReplaceContentAndRetryDispatchesRefresh() {
        val state = mutableStateOf(PickupDetailState())
        val actions = mutableListOf<PickupDetailAction>()
        compose.setContent {
            MangroTheme(typography = OwnerMangroTypography) { PickupDetailScreen(state.value, actions::add) }
        }
        val error = compose.activity.getString(R.string.pickup_error)
        val retry = compose.activity.getString(R.string.owner_management_retry)
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertExists()
        compose.onNodeWithText(error).assertDoesNotExist()
        val loaded = PickupDetailState(
            pickup = Pickup("8", "7", "테스트 상품", "방문손님", 1, 4000, 1, 100000),
            canComplete = true,
        )
        compose.runOnIdle { state.value = loaded }
        compose.onNodeWithText("테스트 상품 * 1개").assertExists()
        compose.runOnIdle { state.value = loaded.copy(isSaving = true) }
        compose.onNodeWithText("테스트 상품 * 1개").assertDoesNotExist()
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertExists()
        compose.runOnIdle { state.value = loaded.copy(hasError = true) }
        compose.onNodeWithText(error).assertIsDisplayed()
        compose.onNodeWithText("테스트 상품 * 1개").assertDoesNotExist()
        compose.onNodeWithText(retry).performClick()
        compose.runOnIdle { assertEquals(listOf(PickupDetailAction.Refresh), actions) }
    }

    @Test fun cancellationLoadingAndFailureDoNotShowEmptyResult() {
        val state = mutableStateOf(PickupCancellationState(isLoading = true))
        val actions = mutableListOf<PickupCancellationAction>()
        compose.setContent {
            MangroTheme(typography = OwnerMangroTypography) { PickupCancellationScreen(state.value, actions::add) }
        }
        val empty = compose.activity.getString(R.string.pickup_cancel_empty)
        val error = compose.activity.getString(R.string.pickup_error)
        val retry = compose.activity.getString(R.string.owner_management_retry)
        compose.onNodeWithText(empty).assertDoesNotExist()
        compose.onNodeWithText(error).assertDoesNotExist()
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertExists()
        compose.runOnIdle { state.value = PickupCancellationState(hasError = true) }
        compose.onNodeWithText(empty).assertDoesNotExist()
        compose.onNodeWithText(error).assertIsDisplayed()
        compose.onNodeWithText(retry).performClick()
        compose.runOnIdle {
            assertEquals(listOf(PickupCancellationAction.Refresh), actions)
            state.value = PickupCancellationState(isSaving = true)
        }
        compose.onNodeWithText(empty).assertDoesNotExist()
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertExists()
        compose.runOnIdle { state.value = PickupCancellationState() }
        compose.onNodeWithText(empty).assertIsDisplayed()
    }
}
