package com.swyp.mangro.core.designsystem.theme.components.badge

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroCaption
import com.swyp.mangro.core.designsystem.theme.LocalMangroColors
import com.swyp.mangro.core.designsystem.theme.OwnerMangroCaption
import com.swyp.mangro.core.designsystem.theme.Red100
import com.swyp.mangro.core.designsystem.theme.Red400

/**
 * 텍스트와 시각 속성을 호출부에서 자유롭게 조합할 수 있는 공용 배지입니다.
 *
 * 비즈니스 상태를 직접 알지 않으며, [text]에는 특정 상태로 제한되지 않은 어떤 문구도 사용할 수 있습니다.
 */
@Composable
fun MangroBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int? = null,
    trailingText: String? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
    iconSize: Dp = 16.dp,
    iconSpacing: Dp = 2.dp,
    trailingTextSpacing: Dp = 2.dp,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(iconSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconResId?.let { drawableResId ->
            Icon(
                painter = painterResource(drawableResId),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = contentColor,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(trailingTextSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                color = contentColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )

            trailingText?.let { value ->
                Text(
                    text = value,
                    color = contentColor,
                    style = textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

@Preview(name = "Badge catalog - 689:3196", showBackground = true, backgroundColor = 0xFFD9D9D9)
@Composable
private fun MangroBadgeCatalogPreview() {
    val colors = LocalMangroColors.current

    Row(
        modifier = Modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MangroBadge(
                text = "여유 있어요",
                containerColor = colors.surfaceNormal,
                contentColor = colors.primaryNormal,
                textStyle = ConsumerMangroCaption.captionS,
                iconResId = R.drawable.ic_alarm_on_16px,
            )
            MangroBadge(
                text = "서둘러 주세요",
                containerColor = colors.surfaceNormal,
                contentColor = Red400,
                textStyle = ConsumerMangroCaption.captionS,
                iconResId = R.drawable.ic_acute,
            )
            MangroBadge(
                text = "아직 2개가 남아 있어요",
                containerColor = colors.surfaceNormal,
                contentColor = colors.textTitle,
                textStyle = ConsumerMangroCaption.captionS,
                iconResId = R.drawable.ic_bag,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MangroBadge(
                    text = "판매중",
                    containerColor = colors.primaryNormal,
                    contentColor = colors.textOnBrandWhite,
                    textStyle = OwnerMangroCaption.captionS,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                )
                MangroBadge(
                    text = "품절",
                    containerColor = colors.surfaceNormal,
                    contentColor = colors.textCanceled,
                    textStyle = OwnerMangroCaption.captionS,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                )
                MangroBadge(
                    text = "마감",
                    containerColor = colors.surfaceDisabled,
                    contentColor = colors.textCanceled,
                    textStyle = OwnerMangroCaption.captionS,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MangroBadge(
                    text = "찜중",
                    containerColor = colors.warningBg,
                    contentColor = colors.primaryNormal,
                    textStyle = ConsumerMangroCaption.captionS,
                )
                MangroBadge(
                    text = "수령완료",
                    containerColor = colors.surfaceAlter,
                    contentColor = colors.textCanceled,
                    textStyle = ConsumerMangroCaption.captionS,
                )
                MangroBadge(
                    text = "만료",
                    containerColor = colors.dangerBg,
                    contentColor = colors.dangerNormal,
                    textStyle = ConsumerMangroCaption.captionS,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MangroBadge(
                    text = "3개 남음",
                    containerColor = colors.warningBg,
                    contentColor = colors.primaryNormal,
                    textStyle = ConsumerMangroCaption.captionS,
                    iconResId = R.drawable.ic_badge_dot_4px,
                    iconSize = 4.dp,
                    iconSpacing = 4.dp,
                )
                MangroBadge(
                    text = "1시간 뒤 마감",
                    containerColor = Red100,
                    contentColor = colors.dangerNormal,
                    textStyle = ConsumerMangroCaption.captionS,
                    iconResId = R.drawable.ic_acute,
                    contentPadding = PaddingValues(start = 7.dp, top = 2.dp, end = 8.dp, bottom = 2.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MangroBadge(
                    text = "오고 있는 손님",
                    trailingText = "3",
                    containerColor = colors.primaryLight,
                    contentColor = colors.primaryNormal,
                    textStyle = OwnerMangroCaption.captionS,
                    iconResId = R.drawable.ic_person_pin_circle_16px,
                    iconSpacing = 4.dp,
                )
            }

            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MangroBadge(
                    text = "취소예정",
                    containerColor = colors.dangerBg,
                    contentColor = colors.dangerNormal,
                    textStyle = ConsumerMangroCaption.captionS,
                )

                MangroBadge(
                    text = "60%",
                    containerColor = colors.primaryNormal,
                    contentColor = colors.textOnBrandWhite,
                    textStyle = ConsumerMangroCaption.captionS,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}
