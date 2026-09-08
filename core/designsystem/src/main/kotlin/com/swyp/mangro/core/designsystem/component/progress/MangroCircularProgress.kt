package com.swyp.mangro.core.designsystem.component.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.Orange700
import com.swyp.mangro.core.designsystem.theme.Orange900

@Composable
fun MangroCircularProgress(
    progress: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    thickness: Dp = 10.dp,
) {
    val trackColor = Color(0xFFD9D9D9)
    val progressBrush = if (isActive) {
        Brush.verticalGradient(colors = listOf(Orange900, Orange700))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFFD9D9D9), Color(0xFFD9D9D9)))
    }
    val normalizedProgress = progress.coerceIn(0f, 1f)

    Canvas(modifier = modifier) {
        val strokeWidth = thickness.toPx()

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round),
        )

        drawArc(
            brush = progressBrush,
            startAngle = -90f,
            sweepAngle = -360f * normalizedProgress,
            useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MangroCircularProgressPreview() {
    MangroTheme {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            MangroCircularProgress(
                progress = 0.7f,
                isActive = true,
                modifier = Modifier.size(56.dp),
            )
            MangroCircularProgress(
                progress = 0.3f,
                isActive = true,
                modifier = Modifier.size(56.dp),
            )
            MangroCircularProgress(
                progress = 0.7f,
                isActive = false,
                modifier = Modifier.size(56.dp),
            )
        }
    }
}
