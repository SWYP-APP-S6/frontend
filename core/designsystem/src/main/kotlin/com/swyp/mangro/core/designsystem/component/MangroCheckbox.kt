package com.swyp.mangro.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.White

private val Boolean.backgroundColor: Color
    @Composable
    get() = if (this) MangroTheme.colors.primaryNormal else White

private val Boolean.borderColor: Color
    @Composable
    get() = if (this) MangroTheme.colors.primaryNormal else MangroTheme.colors.textCanceled

private const val ANIMATION_DURATION = 100

@Composable
fun MangroCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(
        targetState = isChecked,
        label = "checkedTransition",
    )

    val backgroundColor by transition.animateColor(
        transitionSpec = { tween(ANIMATION_DURATION) },
        label = "backgroundColor",
    ) { checked -> checked.backgroundColor }

    val borderColor by transition.animateColor(
        transitionSpec = { tween(ANIMATION_DURATION) },
        label = "borderColor",
    ) { checked -> checked.borderColor }

    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(shape = shape)
                .size(18.dp)
                .background(
                    color = backgroundColor,
                    shape = shape,
                )
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = shape,
                )
                .toggleable(
                    role = Role.Checkbox,
                    value = isChecked,
                    onValueChange = onCheckedChange,
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = isChecked,
                enter = fadeIn(tween(ANIMATION_DURATION)),
                exit = fadeOut(tween(ANIMATION_DURATION)),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_checkbox),
                    contentDescription = null,
                    tint = MangroTheme.colors.textOnBrandWhite,
                )
            }
        }
    }
}

private class MangroCheckboxPreviewProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean>
        get() = sequenceOf(true, false)
}

@Preview
@Composable
private fun MangroCheckboxPreview(
    @PreviewParameter(MangroCheckboxPreviewProvider::class) initialChecked: Boolean,
) {
    MangroTheme {
        var checked by remember { mutableStateOf(initialChecked) }

        Box(
            modifier = Modifier
                .background(White),
        ) {
            MangroCheckbox(
                isChecked = checked,
                onCheckedChange = { checked = it },
            )
        }
    }
}
