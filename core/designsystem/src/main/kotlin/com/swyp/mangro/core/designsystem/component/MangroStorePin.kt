package com.swyp.mangro.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.compose.balloon
import com.skydoves.balloon.compose.rememberBalloonBuilder
import com.skydoves.balloon.compose.rememberBalloonState
import com.skydoves.balloon.compose.setArrowColor
import com.skydoves.balloon.compose.setBackgroundColor
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.Gray400
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.White

sealed interface StorePinState {
    data class Empty(
        val count: Int,
    ) : StorePinState

    data class Default(
        val count: Int,
    ) : StorePinState

    data class Selected(
        val name: String,
        val count: Int,
    ) : StorePinState
}

fun storePinStateOf(
    count: Int,
    name: String,
    isSelected: Boolean,
): StorePinState = when {
    count <= 0 -> StorePinState.Empty(count)
    isSelected -> StorePinState.Selected(name, count)
    else -> StorePinState.Default(count)
}

@Composable
fun MangroStorePin(
    state: StorePinState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is StorePinState.Empty ->
            BasketBadge(isAvailable = false, count = state.count, modifier = modifier)

        is StorePinState.Default ->
            BasketBadge(
                isAvailable = true,
                count = state.count,
                modifier = modifier.clickable(onClick = onTap),
            )

        is StorePinState.Selected ->
            SelectedStorePin(
                name = state.name,
                count = state.count,
                onDismissRequest = onTap,
                modifier = modifier,
            )
    }
}

private val Boolean.badgeBorderColor: Color
    @Composable
    get() = if (this) MangroTheme.colors.primaryNormal else Gray400

private val Boolean.badgeTextColor: Color
    @Composable
    get() = if (this) MangroTheme.colors.grayScale900 else MangroTheme.colors.textSubtitle

private val shape = RoundedCornerShape(percent = 50)

@Composable
private fun BasketBadge(
    isAvailable: Boolean,
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(shape)
            .background(
                color = White,
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = isAvailable.badgeBorderColor,
                shape = shape,
            )
            .padding(
                vertical = 4.dp,
                horizontal = 8.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_bag),
            contentDescription = null,
            tint = isAvailable.badgeBorderColor,
        )

        Text(
            text = "$count",
            color = isAvailable.badgeTextColor,
            style = MangroTheme.typography.label.labelM,
        )
    }
}

@Composable
private fun SelectedStorePin(
    name: String,
    count: Int,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MangroTheme.colors.primaryNormal
    val windowInfo = LocalWindowInfo.current
    var anchorTopPx by remember { mutableStateOf<Float?>(null) }

    val builder = rememberBalloonBuilder {
        setArrowSize(10)
        setArrowPosition(0.5f)
        setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        setArrowColor(primaryColor)
        setPadding(0)
        setCornerRadius(0f)
        setBackgroundColor(Color.Transparent)
        setBalloonAnimation(BalloonAnimation.FADE)
        setDismissWhenTouchOutside(true)
    }
    val balloonState = rememberBalloonState(builder)
    balloonState.setOnBalloonDismissListener { onDismissRequest() }

    BasketBadge(
        isAvailable = true,
        count = count,
        modifier = modifier
            .alpha(0f)
            .onGloballyPositioned { coordinates ->
                anchorTopPx = coordinates.positionInWindow().y
            }
            .balloon(balloonState) {
                Text(
                    text = "$name・${count}개",
                    color = MangroTheme.colors.secondaryNormal,
                    style = MangroTheme.typography.title.titleM,
                    modifier = Modifier
                        .background(color = primaryColor, shape = shape)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            },
    )

    LaunchedEffect(anchorTopPx) {
        val top = anchorTopPx ?: return@LaunchedEffect
        if (top < windowInfo.containerSize.height / 2f) {
            balloonState.showAlignBottom()
        } else {
            balloonState.showAlignTop()
        }
    }
}

private class StorePinStatePreviewParameterProvider : PreviewParameterProvider<StorePinState> {
    override val values = sequenceOf(
        StorePinState.Empty(count = 0),
        StorePinState.Default(count = 5),
    )
}

@Preview(showBackground = true)
@Composable
private fun MangroStorePinPreview(
    @PreviewParameter(StorePinStatePreviewParameterProvider::class) state: StorePinState,
) {
    MangroTheme {
        Box(
            modifier = Modifier
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            MangroStorePin(
                state = state,
                onTap = {},
            )
        }
    }
}
