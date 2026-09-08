package com.swyp.mangro.core.designsystem.component.stepper

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.OwnerMangroTitle
import com.swyp.mangro.core.designsystem.theme.PretendardFont

/**
 * SMALL button 36.dp, gap 12.dp
 * LARGE button 48.dp, gap 24.dp
 */
enum class MangroStepperSize {
    SMALL,
    LARGE,
}

@Composable
fun MangroStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: MangroStepperSize = MangroStepperSize.SMALL,
    minValue: Int = 1,
    maxValue: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
) {
    require(minValue <= maxValue) { "minValue must be less than or equal to maxValue" }
    require(value in minValue..maxValue) { "value must be in minValue..maxValue" }

    val isSmall = size == MangroStepperSize.SMALL
    val canDecrease = enabled && value > minValue
    val canIncrease = enabled && value < maxValue

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (isSmall) 12.dp else 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(
            onClick = { if (canDecrease) onValueChange(value - 1) },
            icon = if (isSmall) R.drawable.ic_stepper_minus_small else R.drawable.ic_stepper_minus_large,
            enabled = canDecrease,
            size = if (isSmall) 36.dp else 48.dp,
        )

        Text(
            text = value.toString(),
            modifier = Modifier.widthIn(min = if (isSmall) 26.dp else 36.dp),
            color = MangroTheme.colors.textTitle,
            style = if (isSmall) {
                OwnerMangroTitle.titleL.copy(fontFamily = PretendardFont.Bold)
            } else {
                MangroTheme.typography.heading.headingL
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )

        StepperButton(
            onClick = { if (canIncrease) onValueChange(value + 1) },
            icon = if (isSmall) R.drawable.ic_stepper_plus_small else R.drawable.ic_stepper_plus_large,
            enabled = canIncrease,
            size = if (isSmall) 36.dp else 48.dp,
        )
    }
}

@Composable
private fun StepperButton(
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val backgroundColor = if (enabled) {
        MangroTheme.colors.primaryStrong
    } else {
        MangroTheme.colors.surfaceNormal
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (enabled) {
                    Modifier
                } else {
                    Modifier.border(1.dp, MangroTheme.colors.borderDefault, CircleShape)
                },
            )
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(size),
            tint = if (enabled) MangroTheme.colors.textOnBrandWhite else Color(0xFFBDBDBD),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun MangroStepperPreview() {
    MangroTheme {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            listOf(1, 2).forEach { initialValue ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MangroStepperSize.entries.forEach { size ->
                        var value by remember { mutableIntStateOf(initialValue) }
                        MangroStepper(
                            value = value,
                            onValueChange = { value = it },
                            size = size,
                        )
                    }
                }
            }
        }
    }
}
