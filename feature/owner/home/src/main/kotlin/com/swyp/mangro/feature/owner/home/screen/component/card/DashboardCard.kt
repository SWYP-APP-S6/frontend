package com.swyp.mangro.feature.owner.home.screen.component.card

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.utils.dropShadow

@Composable
internal fun DashboardCard(
    title: String,
    value: String,
    @DrawableRes drawResId: Int,
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .dropShadow(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.15f),
                blur = 10.dp,
                offsetX = 0.dp,
                offsetY = 0.dp,
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(startColor, endColor)))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MangroTheme.colors.surfaceNormal,
                    shape = CircleShape,
                ).padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(drawResId),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MangroTheme.typography.caption.captionS,
                color = MangroTheme.colors.textBody,
                textAlign = TextAlign.Center,
            )
            Text(
                text = value,
                style = MangroTheme.typography.number.numberL,
                color = MangroTheme.colors.textTitle,
                textAlign = TextAlign.Center,
            )
        }
    }
}
