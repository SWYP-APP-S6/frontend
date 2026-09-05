package com.swyp.mangro.core.designsystem.theme.utils

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dropShadow(
    shape: Shape,
    color: Color = Color.Black.copy(0.25f),
    blur: Dp = 4.dp,
    offsetY: Dp = 4.dp,
    offsetX: Dp = 0.dp,
    spread: Dp = 0.dp,
): Modifier {
    // API 28 이상부터 Compose BlurMask Shader의 하드웨어 가속이 지원됨
    return this.drawBehind {
        val shadowSize = Size(size.width + spread.toPx(), size.height + spread.toPx())
        val shadowOutline = shape.createOutline(shadowSize, layoutDirection, this)

        val paint = Paint()
        paint.color = color

        if (blur.toPx() > 0) {
            paint.asFrameworkPaint().apply {
                maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
        }

        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.translate(offsetX.toPx(), offsetY.toPx())
            canvas.drawOutline(shadowOutline, paint)
            canvas.restore()
        }
    }
}
