package com.swyp.mangro

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.image.MangroImagePageController
import com.swyp.mangro.core.designsystem.component.image.MangroImagePageDots
import com.swyp.mangro.core.designsystem.component.image.MangroImagePageNumbers
import com.swyp.mangro.core.designsystem.component.image.MangroImageViewer
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test

class MangroImageViewerTest {
    @get:Rule
    val compose = createComposeRule()

    private val images = persistentListOf<Any>(
        DesignR.drawable.preview_slider_peach,
        DesignR.drawable.preview_slider_detail,
        DesignR.drawable.preview_slider_basket,
    )

    @Test
    fun bothControllersShowAllFiveStatesAndHideForSingleImage() {
        compose.setContent {
            MangroTheme {
                Column {
                    repeat(5) {
                        MangroImagePageDots(it, 5)
                        MangroImagePageNumbers(it, 5)
                    }
                    MangroImagePageDots(0, 1)
                    MangroImagePageNumbers(0, 1)
                }
            }
        }
        repeat(5) {
            compose.onAllNodesWithContentDescription("전체 5장 중 ${it + 1}번째 이미지").assertCountEquals(2)
        }
        compose.onAllNodesWithContentDescription("전체 1장 중 1번째 이미지").assertCountEquals(0)
    }

    @Test
    fun manualSwipesLoopInBothDirectionsWithNumbers() {
        compose.setContent {
            MangroTheme { MangroImageViewer(images, autoScrollEnabled = false) }
        }
        assertControllerPage(1)
        compose.onNode(hasScrollAction()).performTouchInput { swipeRight() }
        assertControllerPage(3)
        compose.onNode(hasScrollAction()).performTouchInput { swipeLeft() }
        assertControllerPage(1)
        repeat(3) { compose.onNode(hasScrollAction()).performTouchInput { swipeLeft() } }
        assertControllerPage(1)
    }

    @Test
    fun autoScrollWaitsThreeSecondsAndWrapsWithDots() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MangroTheme { MangroImageViewer(images, controller = MangroImagePageController.Dots) }
        }
        compose.mainClock.advanceTimeBy(100)
        assertControllerPage(1)
        compose.mainClock.advanceTimeBy(2_800)
        assertControllerPage(1)
        compose.mainClock.advanceTimeBy(1_200)
        assertControllerPage(2)
        compose.mainClock.advanceTimeBy(3_500)
        assertControllerPage(3)
        compose.mainClock.advanceTimeBy(3_500)
        assertControllerPage(1)
    }

    @Test
    fun imageListCanChangeToSingleAndEmpty() {
        val models = mutableStateOf(images)
        compose.setContent {
            MangroTheme { MangroImageViewer(models.value, Modifier.testTag("viewer"), autoScrollEnabled = false) }
        }
        compose.onNode(hasScrollAction()).performTouchInput { swipeLeft() }
        assertControllerPage(2)
        compose.runOnIdle { models.value = persistentListOf(images.first()) }
        compose.onAllNodesWithContentDescription("전체 1장 중 1번째 이미지").assertCountEquals(1)
        compose.runOnIdle { models.value = persistentListOf() }
        compose.onNodeWithTag("viewer").assertExists()
        compose.onAllNodesWithContentDescription("전체 1장 중 1번째 이미지").assertCountEquals(0)
        compose.runOnIdle { models.value = images }
        assertControllerPage(1)
    }

    private fun assertControllerPage(page: Int) {
        // 이미지에도 설명이 있으므로, 별도 컨트롤러 노드가 함께 존재하는지 확인합니다.
        compose.onAllNodes(hasContentDescription("전체 3장 중 ${page}번째 이미지")).assertCountEquals(2)
    }
}
