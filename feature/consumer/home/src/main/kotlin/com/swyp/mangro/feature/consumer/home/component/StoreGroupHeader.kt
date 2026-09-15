package com.swyp.mangro.feature.consumer.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.badge.MangroBadge
import com.swyp.mangro.core.designsystem.theme.Gray300
import com.swyp.mangro.core.designsystem.theme.Gray50
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.Red50
import com.swyp.mangro.feature.consumer.home.R

@Composable
internal fun StoreGroupHeader(
    storeName: String,
    walkingMinutes: Int,
    productCount: Int,
    closingInMinutes: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Gray300,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 0.6.dp.toPx(),
                )
            }
            .background(Gray50)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = storeName,
            style = MangroTheme.typography.title.titleL,
            color = MangroTheme.colors.textTitle,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(R.string.home_store_walk, walkingMinutes),
            style = MangroTheme.typography.caption.captionS,
            color = MangroTheme.colors.textCanceled,
        )

        Spacer(modifier = Modifier.width(4.dp))

        VerticalDivider(
            modifier = Modifier
                .height(6.dp)
                .width(1.dp),
            color = Color(0xFFD9D9D9),
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = stringResource(R.string.home_store_count, productCount),
            style = MangroTheme.typography.caption.captionS,
            color = MangroTheme.colors.textCanceled,
        )

        Spacer(modifier = Modifier.weight(1f))

        closingInMinutes?.let { minutes ->
            MangroBadge(
                iconResId = com.swyp.mangro.core.designsystem.R.drawable.ic_acute,
                text = if (minutes < 60) {
                    stringResource(R.string.home_closing_in_minutes, minutes)
                } else {
                    stringResource(R.string.home_closing_in_hours, minutes / 60)
                },
                containerColor = Red50,
                contentColor = MangroTheme.colors.dangerNormal,
                textStyle = MangroTheme.typography.label.labelXS ?: MangroTheme.typography.label.labelM,
            )
        }
    }
}

private class StoreGroupHeaderPreviewProvider : PreviewParameterProvider<StoreGroupHeaderPreviewParam> {
    override val values: Sequence<StoreGroupHeaderPreviewParam>
        get() = sequenceOf(
            StoreGroupHeaderPreviewParam(
                storeName = "청과마을",
                walkingMinutes = 7,
                productCount = 3,
                closingInMinutes = 60,
            ),
            StoreGroupHeaderPreviewParam(
                storeName = "정육점",
                walkingMinutes = 10,
                productCount = 1,
                closingInMinutes = null,
            ),
            StoreGroupHeaderPreviewParam(
                storeName = "동네수산",
                walkingMinutes = 3,
                productCount = 12,
                closingInMinutes = 15,
            ),
        )
}

private data class StoreGroupHeaderPreviewParam(
    val storeName: String,
    val walkingMinutes: Int,
    val productCount: Int,
    val closingInMinutes: Int?,
)

@Preview(showBackground = true)
@Composable
private fun StoreGroupHeaderPreview(
    @PreviewParameter(StoreGroupHeaderPreviewProvider::class) param: StoreGroupHeaderPreviewParam,
) {
    MangroTheme {
        StoreGroupHeader(
            storeName = param.storeName,
            walkingMinutes = param.walkingMinutes,
            productCount = param.productCount,
            closingInMinutes = param.closingInMinutes,
            modifier = Modifier.padding(16.dp),
        )
    }
}
