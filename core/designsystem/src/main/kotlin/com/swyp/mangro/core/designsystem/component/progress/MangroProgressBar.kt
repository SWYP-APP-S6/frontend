package com.swyp.mangro.core.designsystem.component.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.MangroTheme

/**
 * 0~1 범위의 [progress]를 표시한다. 범위를 벗어난 값은 보정하며 NaN은 0으로 처리한다.
 */
@Composable
fun MangroProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    thickness: Dp = 12.dp,
    trackColor: Color = MangroTheme.colors.grayScale50,
    progressColor: Color = MangroTheme.colors.primaryNormal,
) {
    val normalizedProgress = if (progress.isNaN()) 0f else progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .clip(RoundedCornerShape(percent = 50))
            .background(trackColor),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(normalizedProgress)
                .fillMaxHeight()
                .background(progressColor, RoundedCornerShape(percent = 50)),
        )
    }
}

@Preview
@Composable
private fun MangroProgressBarPreview() {
    MangroTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .background(MangroTheme.colors.surfaceNormal)
                    .padding(20.dp)
                    .width(327.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MangroProgressBar(progress = 0f)
                MangroProgressBar(progress = 108f / 327f)
                MangroProgressBar(progress = 1f)
            }
        }
    }
}
