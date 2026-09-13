package com.swyp.mangro.feature.owner.home.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.Red100
import com.swyp.mangro.feature.owner.home.R
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeAction

@Composable
internal fun AttentionCard(
    cancellationRequiredCount: Int,
    needsPickupConfirmation: Boolean,
    modifier: Modifier = Modifier,
    onAction: (OwnerHomeAction) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = Red100,
                shape = RoundedCornerShape(16.dp),
            )
            .background(color = MangroTheme.colors.dangerBg)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_error),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MangroTheme.colors.dangerNormal,
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = { onAction(OwnerHomeAction.DismissAttention) })
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_x_20px),
                    contentDescription = null,
                    tint = MangroTheme.colors.dangerNormal,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = stringResource(R.string.owner_home_attention),
            style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
            color = MangroTheme.colors.dangerNormal,
        )
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MangroTheme.colors.surfaceNormal.copy(alpha = 0.5f)),
        ) {
            if (cancellationRequiredCount > 0) {
                AttentionLink(
                    content = stringResource(R.string.owner_home_cancellation, cancellationRequiredCount),
                    onClick = { onAction(OwnerHomeAction.ViewCancellations) },
                )
            }
            if (needsPickupConfirmation) {
                AttentionLink(
                    content = stringResource(R.string.owner_home_confirmation),
                    onClick = { onAction(OwnerHomeAction.ConfirmPickups) },
                )
            }
        }
    }
}

@Composable
private fun AttentionLink(content: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = content,
            modifier = Modifier.weight(1f),
            style = MangroTheme.typography.caption.captionS,
            color = MangroTheme.colors.textBody,
        )
        Icon(
            painter = painterResource(DesignR.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MangroTheme.colors.dangerNormal,
        )
    }
}
