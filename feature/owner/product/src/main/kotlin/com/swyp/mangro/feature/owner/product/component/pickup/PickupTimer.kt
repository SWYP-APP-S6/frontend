package com.swyp.mangro.feature.owner.product.component.pickup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.card.timer.formatRemaining
import com.swyp.mangro.core.designsystem.component.progress.MangroCircularProgress
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R

@Composable
internal fun PickupTimer(
    remaining: Long,
    duration: Long,
    active: Boolean,
    modifier: Modifier = Modifier,
    inactiveColor: Color = Color(0xFFFFE8A3),
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(92.dp), contentAlignment = Alignment.Center) {
            MangroCircularProgress(
                progress = if (active) (1f - remaining.toFloat() / duration.coerceAtLeast(1)).coerceIn(0f, 1f) else 0f,
                isActive = active,
                modifier = Modifier.fillMaxSize().padding(5.dp),
                trackColor = if (active) Color(0xFFEAEAEA) else inactiveColor,
                inactiveColor = inactiveColor,
            )
            Text(
                text = formatRemaining(remaining),
                style = MangroTheme.typography.title.titleL,
                color = MangroTheme.colors.textTitle,
            )
        }
        Text(
            text = stringResource(R.string.pickup_remaining),
            style = MangroTheme.typography.caption.captionS,
            color = MangroTheme.colors.textCanceled,
        )
    }
}
