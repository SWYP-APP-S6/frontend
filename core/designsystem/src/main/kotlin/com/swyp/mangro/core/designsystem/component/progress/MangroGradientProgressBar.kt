package com.swyp.mangro.core.designsystem.component.progress

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.Orange100
import com.swyp.mangro.core.designsystem.theme.Orange600
import com.swyp.mangro.core.designsystem.theme.Red300
import com.swyp.mangro.core.designsystem.theme.Red50
import com.swyp.mangro.core.designsystem.theme.Red500
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/**
 * [height]를 전체 높이로 사용하는 진행률 표시. 기본 24dp에서 트랙 두께는 8dp이다.
 * 실제 측정 높이의 1/3을 트랙 두께로 사용하고, 마커 최대 지름은 실제 너비와 높이 중 작은 값이다.
 * 마커를 숨겨도 전체 높이와 트랙 두께의 비율은 유지한다.
 *
 * 표시 중인 진행률이 [thresholdValue]를 초과하면 그라데이션과 마커가 기준값 이후 색상으로 전환된다.
 *
 * [progress]에는 목표 진행률을 전달한다.
 * 목표 진행률 변경 시 300ms 동안 부드럽게 전환한다.
 *
 * 0~1 범위를 벗어난 값은 보정하고 NaN은 0으로 처리한다.
 * 마커 중심은 진행 끝점과 일치하며, 양 끝에서는 마커 반지름만큼 영역 밖으로 그려진다.
 */
@Composable
fun MangroGradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 24.dp,
    isMarkerVisible: Boolean = true,
    isDividerVisible: Boolean = true,
    thresholdValue: Float = 0.66f,
    trackColor: Color = MangroTheme.colors.surfaceNormal,
    dividerColor: Color = MangroTheme.colors.secondaryNormal.copy(alpha = 0.4f),
    initialStartColor: Color = Orange600,
    initialEndColor: Color = MangroTheme.colors.primaryNormal,
    thresholdPassedStartColor: Color = Red300,
    thresholdPassedEndColor: Color = Red500,
) {
    val normalizedProgress = if (progress.isNaN()) 0f else progress.coerceIn(0f, 1f)
    val animatedProgress = animateFloatAsState(
        targetValue = normalizedProgress,
        animationSpec = tween(durationMillis = 300),
    )
    val isDanger by remember(animatedProgress, thresholdValue) {
        derivedStateOf { animatedProgress.value > thresholdValue }
    }
    val animatedStartColor = animateColorAsState(
        targetValue = if (isDanger) thresholdPassedStartColor else initialStartColor,
        label = "Progress start color",
    )
    val animatedEndColor = animateColorAsState(
        targetValue = if (isDanger) thresholdPassedEndColor else initialEndColor,
        label = "Progress end color",
    )
    val transition = rememberInfiniteTransition(label = "Progress marker breathing")
    val markerScale = transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas

        val fraction = animatedProgress.value
        val trackHeight = size.height / 3f
        val top = (size.height - trackHeight) / 2f
        val cornerRadius = CornerRadius(trackHeight / 2f)
        val fillWidth = size.width * fraction

        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, top),
            size = Size(size.width, trackHeight),
            cornerRadius = cornerRadius,
        )

        if (fillWidth > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(animatedStartColor.value, animatedEndColor.value),
                    startX = 0f,
                    endX = fillWidth,
                ),
                topLeft = Offset(0f, top),
                size = Size(fillWidth, trackHeight),
                cornerRadius = cornerRadius,
            )
        }

        if (isDividerVisible) {
            val dividerWidth = 1.dp.toPx().coerceAtMost(size.width)
            for (index in 1..2) {
                drawRect(
                    color = dividerColor,
                    topLeft = Offset(size.width * index / 3f - dividerWidth / 2f, top),
                    size = Size(dividerWidth, trackHeight),
                )
            }
        }

        if (isMarkerVisible) {
            val markerMaxSize = minOf(size.width, size.height)
            val markerRadius = markerMaxSize * markerScale.value / 2f
            val markerCenter = Offset(fillWidth, size.height / 2f)

            mangroGradientProgressBarMarker(
                color = animatedEndColor.value,
                radius = markerRadius,
                center = markerCenter,
            )
        }
    }
}

private fun DrawScope.mangroGradientProgressBarMarker(color: Color, radius: Float, center: Offset) {
    drawCircle(
        color = color,
        radius = radius,
        center = center,
        alpha = 0.2f,
    )

    drawCircle(
        color = color,
        // 기존 마커의 안쪽 원과 바깥 원 반지름 비율을 유지한다.
        radius = radius * (5.616f / 11.856f),
        center = center,
    )
}

@Preview(showBackground = true)
@Composable
private fun MangroGradientProgressBarSizePreview() {
    MangroTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.width(330.dp).background(Orange100).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                MangroGradientProgressBar(progress = 0.5f, height = 12.dp)
                MangroGradientProgressBar(progress = 0.5f, height = 24.dp)
                MangroGradientProgressBar(progress = 0.5f, height = 36.dp)
                MangroGradientProgressBar(progress = 0f, height = 36.dp)
                MangroGradientProgressBar(progress = 1f, height = 36.dp)
                MangroGradientProgressBar(
                    progress = 0.5f,
                    height = 36.dp,
                    isMarkerVisible = false,
                    initialStartColor = Red300,
                    initialEndColor = Red500,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MangroGradientProgressBarAnimationPreview() {
    var progress by remember { mutableFloatStateOf(0.5f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_500.milliseconds)
            progress = if (progress == 0.5f) 0.8f else 0.5f
        }
    }
    MangroTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            MangroGradientProgressBar(
                progress = progress,
                modifier = Modifier.width(330.dp).background(Orange100).padding(20.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MangroGradientProgressBarPreview() {
    MangroTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(20.dp).width(330.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.background(Orange100).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    MangroGradientProgressBar(progress = 0f)
                    MangroGradientProgressBar(progress = 0.25f)
                    MangroGradientProgressBar(progress = 0.65f, isDividerVisible = false)
                }
                Column(
                    modifier = Modifier.background(Red50).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    MangroGradientProgressBar(progress = 0.7501f)
                    MangroGradientProgressBar(progress = 0.9f)
                    MangroGradientProgressBar(progress = 1f)
                }
            }
        }
    }
}
