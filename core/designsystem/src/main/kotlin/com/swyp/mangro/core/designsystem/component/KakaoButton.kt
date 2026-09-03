package com.swyp.mangro.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.White

private object KakaoColor {
    val Background = Color(0xFFF9E007)
    val Text = Color(0xFF3B1E1E)
}

@Composable
fun KakaoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(KakaoColor.Background)
            .clickable(
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(
                vertical = 16.dp,
                horizontal = 6.dp,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_kakao),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.kakao_login),
            color = KakaoColor.Text,
            style = MangroTheme.typography.heading.headingXXS,
        )
    }
}

@Preview
@Composable
private fun KakaoButtonPreview() {
    MangroTheme {
        Box(
            modifier = Modifier
                .background(White)
                .padding(20.dp),
        ) {
            KakaoButton(
                onClick = {},
            )
        }
    }
}
