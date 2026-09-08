package com.swyp.mangro.core.designsystem.component.label

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.swyp.mangro.core.designsystem.theme.Gray200
import com.swyp.mangro.core.designsystem.theme.Gray700
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.Orange50
import com.swyp.mangro.core.designsystem.theme.Orange900
import com.swyp.mangro.core.designsystem.theme.Red50
import com.swyp.mangro.core.designsystem.theme.Red600

@Composable
fun MangroLabel(
    content: String,
    contentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(4.dp))
            .background(
                color = containerColor,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(
                horizontal = 8.dp,
                vertical = 2.dp,
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

@Composable
fun MangroLabel(
    content: String,
    labelTheme: MangroLabelTheme,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(4.dp))
            .background(
                color = labelTheme.containerColor,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(
                horizontal = 8.dp,
                vertical = 2.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = content,
            color = labelTheme.contentColor,
            style = MangroTheme.typography.caption.captionS,
        )
    }
}

enum class MangroLabelTheme(val containerColor: Color, val contentColor: Color) {
    PRIMARY(Orange50, Orange900),
    SURFACE(Gray200, Gray700),
    DANGER(Red50, Red600),
}

@Preview(showBackground = true)
@Composable
fun MangroLabelPreview() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MangroLabel(
            "Label",
            MangroLabelTheme.PRIMARY,
        )

        MangroLabel(
            "Label",
            MangroLabelTheme.SURFACE,
        )

        MangroLabel(
            "Label",
            MangroLabelTheme.DANGER,
        )

        MangroLabel(
            content = "곡류",
            contentColor = MangroTheme.colors.grainsNormal,
            containerColor = MangroTheme.colors.grainsBg,
        )

        MangroLabel(
            content = "과채류",
            contentColor = MangroTheme.colors.vegetablesNormal,
            containerColor = MangroTheme.colors.vegetablesBg,
        )

        MangroLabel(
            content = "육류",
            contentColor = MangroTheme.colors.meatNormal,
            containerColor = MangroTheme.colors.meatBg,
        )

        MangroLabel(
            content = "어류",
            contentColor = MangroTheme.colors.seafoodNormal,
            containerColor = MangroTheme.colors.seafoodBg,
        )

        MangroLabel(
            content = "견과류",
            contentColor = MangroTheme.colors.nutsNormal,
            containerColor = MangroTheme.colors.nutsBg,
        )

        MangroLabel(
            content = "기타",
            contentColor = MangroTheme.colors.textSubtitle,
            containerColor = MangroTheme.colors.surfaceDisabled,
        )
    }
}
