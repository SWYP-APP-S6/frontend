package com.swyp.mangro.feature.consumer.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.card.timer.rememberTimerCardState
import com.swyp.mangro.core.designsystem.theme.Gray600
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
internal fun CountdownCard(
    storeName: String,
    productSummary: String,
    requestTimeMillis: Long,
    endTimeMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timerState = rememberTimerCardState(requestTimeMillis, endTimeMillis)
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = Color(0x4DA6A6A6),
                spotColor = Color(0x4DA6A6A6),
            )
            .clip(shape)
            .background(MangroTheme.colors.textOnBrandWhite)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(
                top = 12.dp,
                bottom = 12.dp,
                start = 16.dp,
                end = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_store_front),
                    contentDescription = null,
                    tint = MangroTheme.colors.borderFocus,
                    modifier = Modifier.size(16.dp),
                )

                Spacer(modifier = Modifier.width(2.dp))

                Text(
                    text = storeName,
                    style = MangroTheme.typography.caption.captionM ?: MangroTheme.typography.caption.captionS,
                    color = Gray600,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = productSummary,
                style = MangroTheme.typography.body.bodyL,
                color = MangroTheme.colors.grayScale900,
            )
        }

        Text(
            text = formatRemainingClock(timerState.remainingMillis),
            style = MangroTheme.typography.heading.headingS,
            color = MangroTheme.colors.primaryNormal,
        )

        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MangroTheme.colors.primaryNormal,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun formatRemainingClock(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private class CountdownCardPreviewProvider : PreviewParameterProvider<Long> {
    override val values: Sequence<Long>
        get() = sequenceOf(
            9 * 60 * 1000L + 24 * 1000L,
            2 * 60 * 1000L,
            30 * 1000L,
        )
}

@Preview(showBackground = true)
@Composable
private fun CountdownCardPreview(
    @PreviewParameter(CountdownCardPreviewProvider::class) remainingMillis: Long,
) {
    val now = System.currentTimeMillis()

    MangroTheme {
        CountdownCard(
            storeName = "청과 마을",
            productSummary = "복숭아 4입 · 1개",
            requestTimeMillis = now - 5 * 60 * 1000,
            endTimeMillis = now + remainingMillis,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
