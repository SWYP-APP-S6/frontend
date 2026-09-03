package com.swyp.mangro.core.designsystem.theme.components.tab

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroBody
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroLabel
import com.swyp.mangro.core.designsystem.theme.LocalMangroColors
import com.swyp.mangro.core.designsystem.theme.utils.dropShadow

@Composable
fun MangroPillTabItem(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = LocalMangroColors.current
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) colors.grayScale700 else colors.surfaceAlter,
        label = "PillTabItemContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) colors.textOnBrandWhite else colors.grayScale500,
        label = "PillTabItemContentColor",
    )
    val shadowModifier = if (isSelected) {
        Modifier.dropShadow(
            shape = CircleShape,
            color = Color(0x26909090),
            blur = 3.dp,
            offsetX = 1.dp,
            offsetY = 1.dp,
        )
    } else {
        Modifier
    }

    Text(
        text = text,
        modifier = modifier
            .then(shadowModifier)
            .clip(CircleShape)
            .background(containerColor)
            .selectable(
                selected = isSelected,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = contentColor,
        style = if (isSelected) ConsumerMangroLabel.label02 else ConsumerMangroBody.body02,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Clip,
    )
}

@Composable
fun MangroTabItem(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = LocalMangroColors.current
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) colors.grayScale900 else colors.textSubtitle,
        label = "TabItemContentColor",
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (isSelected) colors.primaryNormal else Color.Transparent,
        label = "TabItemIndicatorColor",
    )

    Text(
        text = text,
        modifier = modifier
            .background(colors.surfaceNormal)
            .drawBehind {
                val indicatorHeight = 2.dp.toPx()

                drawRect(
                    color = indicatorColor,
                    topLeft = Offset(x = 0f, y = size.height - indicatorHeight),
                )
            }
            .selectable(
                selected = isSelected,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = contentColor,
        style = if (isSelected) ConsumerMangroLabel.label02 else ConsumerMangroBody.body02,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Clip,
    )
}

@Preview(showBackground = true)
@Composable
private fun MangroPillTabPreview() {
    val colors = LocalMangroColors.current

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.surfaceAlter)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MangroPillTabItem(
            text = "지도",
            isSelected = true,
            onClick = {},
        )
        MangroPillTabItem(
            text = "목록",
            isSelected = false,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MangroTabItemPreview() {
    Row(modifier = Modifier.selectableGroup()) {
        MangroTabItem(
            text = "거리순",
            isSelected = true,
            onClick = {},
        )
        MangroTabItem(
            text = "거리순",
            isSelected = false,
            onClick = {},
        )
    }
}
