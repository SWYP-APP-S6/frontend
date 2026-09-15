package com.swyp.mangro.feature.consumer.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.LocalMangroColors
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
internal fun HomeCategoryChip(
    content: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMangroColors.current

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) colors.primaryNormal else colors.surfaceNormal,
        label = "HomeCategoryChipContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) colors.textOnBrandWhite else colors.textSubtitle,
        label = "HomeCategoryChipContentColor",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.primaryNormal else colors.borderDefault,
        label = "HomeCategoryChipBorderColor",
    )

    Text(
        text = content,
        color = contentColor,
        style = MangroTheme.typography.body.bodyM,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(50),
            )
            .selectable(
                selected = isSelected,
                onClick = onClick,
            )
            .padding(
                horizontal = 16.dp,
                vertical = 6.dp,
            ),
    )
}
