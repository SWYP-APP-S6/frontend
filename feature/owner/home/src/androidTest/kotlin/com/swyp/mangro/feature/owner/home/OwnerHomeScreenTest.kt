package com.swyp.mangro.feature.owner.home

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTypography
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeAction
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeScreen
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeUiState
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeViewModel
import com.swyp.mangro.feature.owner.home.screen.component.card.VisitorCard
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OwnerHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun firstRegistrationShowsWelcomeAndSendsRegisterAction() {
        val actions = mutableListOf<OwnerHomeAction>()
        showScreen(OwnerHomeSamples.welcome, onAction = actions::add)
        composeRule.onNodeWithText("안녕하세요, 사장님!").assertIsDisplayed()
        capture("01-welcome")
        scrollTo(0)
        composeRule.onNodeWithText("첫 상품 등록하러 가기").performClick()
        assertEquals(listOf(OwnerHomeAction.RegisterProduct), actions)
    }

    @Test
    fun registeredStoreWithEmptyInventoryShowsEmptySections() {
        showScreen(OwnerHomeSamples.empty)
        composeRule.onNodeWithText("지금 확인해야 할 문제는 없어요.").assertIsDisplayed()
        composeRule.onNodeWithText("지금은 방문 예정인 손님이 없어요.").assertIsDisplayed()
        capture("02-empty")
        scrollTo(5)
        composeRule.onNodeWithText("상품 등록하기").assertIsDisplayed()
        composeRule.onNodeWithText("첫 상품 등록하러 가기").assertDoesNotExist()
    }

    @Test
    fun operatingScreenDismissesNoticesWithoutClearingUnderlyingIssues() {
        val viewModel = OwnerHomeViewModel()
        var state by mutableStateOf(viewModel.uiState.value)
        composeRule.setContent {
            MangroTheme(typography = OwnerMangroTypography) {
                OwnerHomeScreen(state) { action ->
                    viewModel.handleAction(action)
                    state = viewModel.uiState.value
                }
            }
        }
        composeRule.onNodeWithText("새로운 찜이 생겼어요").assertIsDisplayed()
        composeRule.onNodeWithText("취소 안내가 필요한 찜이 2건 있어요.").assertIsDisplayed()
        capture("03-operating")

        composeRule.onNodeWithContentDescription("확인 필요 안내 닫기").performClick()
        composeRule.onNodeWithText("확인이 필요한 문제가 있어요.").assertDoesNotExist()
        composeRule.onNodeWithText("지금 확인해야 할 문제는 없어요.").assertDoesNotExist()
        assertTrue(viewModel.uiState.value.hasAttention)
        capture("04-attention-dismissed")

        composeRule.onNodeWithText("확인하기").performClick()
        composeRule.onNodeWithText("새로운 찜이 생겼어요").assertDoesNotExist()
        scrollTo(3)
        composeRule.onNodeWithText("윤지현 님").assertIsDisplayed()
        capture("05-visitors")
        scrollTo(5)
        composeRule.onAllNodesWithText("시금치 한 단").onLast().assertIsDisplayed()
        capture("06-products")
    }

    @Test
    fun visitorCardsKeepSizeAndCountdownAlignmentAndDisableExpiredPickup() {
        val visitors = OwnerHomeSamples.operating(0).visitors.take(2)
        var nowMillis by mutableLongStateOf(0)
        val actions = mutableListOf<OwnerHomeAction>()
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1f)) {
                MangroTheme(typography = OwnerMangroTypography) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Row(
                            Modifier.background(MangroTheme.colors.surfaceAlter).padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            visitors.forEach { visitor ->
                                VisitorCard(nowMillis, visitor, Modifier.testTag(visitor.id), actions::add)
                            }
                        }
                    }
                }
            }
        }
        val offsets = visitors.map { visitor ->
            val card = composeRule.onNodeWithTag(visitor.id)
                .assertWidthIsEqualTo(150.dp)
                .assertHeightIsEqualTo(209.dp)
                .fetchSemanticsNode().boundsInRoot
            val countdown = composeRule.onNode(
                hasText("8분 남음") and hasAnyAncestor(hasTestTag(visitor.id)),
                useUnmergedTree = true,
            ).fetchSemanticsNode().boundsInRoot
            countdown.bottom - card.top
        }
        assertEquals(offsets[0], offsets[1], 1f)
        capture("07-visitor-cards")
        val completeButton = composeRule.onNode(
            hasText("픽업 완료") and hasAnyAncestor(hasTestTag(visitors.first().id)),
        )
        composeRule.runOnIdle { nowMillis = 480_000 }
        completeButton.assertIsNotEnabled()
        capture("08-expired-cards")
        completeButton.performClick()
        assertTrue(actions.isEmpty())
        composeRule.runOnIdle { nowMillis = 0 }
        completeButton.assertIsEnabled().performClick()
        assertEquals(listOf(OwnerHomeAction.CompletePickup(visitors.first().id)), actions)
    }

    @Test
    fun longVisitorTextRemainsVisibleWithLargeFont() {
        val visitor = OwnerHomeSamples.operating(0).visitors[1]
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 1.3f)) {
                MangroTheme(typography = OwnerMangroTypography) {
                    Column(Modifier.verticalScroll(rememberScrollState()).background(MangroTheme.colors.surfaceAlter).padding(16.dp)) {
                        VisitorCard(0, visitor, onAction = {})
                    }
                }
            }
        }
        composeRule.onNodeWithText("8분 남음", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("픽업 완료").assertIsDisplayed()
        capture("09-large-font")
    }

    private fun showScreen(state: OwnerHomeUiState, onAction: (OwnerHomeAction) -> Unit = {}) {
        composeRule.setContent {
            MangroTheme(typography = OwnerMangroTypography) {
                OwnerHomeScreen(state, onAction)
            }
        }
    }

    private fun scrollTo(index: Int) {
        composeRule.onAllNodes(hasScrollToIndexAction()).onFirst().performScrollToIndex(index)
    }

    private fun capture(name: String) {
        val outputDirectory = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val directory = if (outputDirectory != null) {
            File(outputDirectory, "owner-home-screenshots")
        } else {
            File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "owner-home-screenshots")
        }
        directory.mkdirs()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        File(directory, "$name.png").outputStream().use { output ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }
}
