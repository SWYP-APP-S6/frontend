package com.swyp.mangro.core.designsystem.component.label

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun MangroOutlinedLabel(
    content: String,
    modifier: Modifier = Modifier,
    borderColor: Color = MangroTheme.colors.borderDefault,
    contentColor: Color = MangroTheme.colors.textTitle,
) {
    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape,
            )
            .padding(
                vertical = 2.dp,
                horizontal = 8.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = content,
            color = contentColor,
            style = MangroTheme.typography.caption.captionS,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MangroOutlinedLabelPreview() {
    MangroTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MangroOutlinedLabel(content = "복숭아 300g")
                MangroOutlinedLabel(content = "복숭아 300g")
                MangroOutlinedLabel(content = "복숭아 300g")
                MangroOutlinedLabel(content = "복숭아 300g")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MangroOutlinedLabel(content = "열량")
                MangroOutlinedLabel(content = "탄수화물")
                MangroOutlinedLabel(content = "단백질")
                MangroOutlinedLabel(content = "지방")
                MangroOutlinedLabel(content = "나트륨")
            }
        }
    }
}
