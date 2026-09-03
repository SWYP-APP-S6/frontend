package com.swyp.mangro.core.designsystem.theme.components.chip

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroBody
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroLabel
import com.swyp.mangro.core.designsystem.theme.LocalMangroColors
import com.swyp.mangro.core.designsystem.theme.OwnerMangroLabel

@Composable
fun MangroChip(
    isOwner: Boolean,
    content: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    @DrawableRes drawResId: Int? = null,
    onClick: () -> Unit,
) {
    val colors = LocalMangroColors.current
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) colors.grayScale700 else colors.surfaceNormal,
        label = "ChipContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            !isSelected -> colors.textSubtitle
            isOwner -> colors.grayScale50
            else -> colors.textOnBrandWhite
        },
        label = "ChipContentColor",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.grayScale700 else colors.borderDefault,
        label = "ChipBorderColor",
    )

    Row(
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
                horizontal = if (isOwner) 16.dp else 12.dp,
                vertical = 4.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        drawResId?.let { iconResId ->
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor,
            )
        }

        Text(
            text = content,
            color = contentColor,
            style = chipTextStyle(isOwner = isOwner, isSelected = isSelected),
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

private fun chipTextStyle(
    isOwner: Boolean,
    isSelected: Boolean,
): TextStyle = when {
    isOwner -> OwnerMangroLabel.label03 ?: OwnerMangroLabel.label02
    isSelected -> ConsumerMangroLabel.label02
    else -> ConsumerMangroBody.body02
}

@Preview(showBackground = true)
@Composable
fun MangroChipPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MangroChip(
            isOwner = false,
            content = "전체",
            isSelected = true,
        ) { }

        MangroChip(
            isOwner = false,
            content = "전체",
            isSelected = false,
        ) { }

        MangroChip(
            isOwner = true,
            content = "전체",
            isSelected = false,
            drawResId = R.drawable.ic_copy,
        ) { }

        MangroChip(
            isOwner = true,
            content = "전체",
            isSelected = true,
            drawResId = R.drawable.ic_copy,
        ) { }
    }
}
