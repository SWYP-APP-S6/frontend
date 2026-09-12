package com.swyp.mangro.feature.owner.home.screen.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.home.R
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeAction
import com.swyp.mangro.feature.owner.home.screen.OwnerHomeVisitor
import com.swyp.mangro.feature.owner.home.screen.utils.remainingPickupMinutes

@Composable
internal fun VisitorCard(
    nowMillis: Long,
    visitor: OwnerHomeVisitor,
    modifier: Modifier = Modifier,
    onAction: (OwnerHomeAction) -> Unit,
) {
    val minutes = remainingPickupMinutes(visitor.pickupDeadlineMillis, nowMillis)

    Column(
        modifier = modifier
            .width(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MangroTheme.colors.surfaceNormal),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 165.dp)
                .clickable(onClick = { onAction(OwnerHomeAction.ViewPickup(visitor.id)) })
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.owner_home_customer, visitor.customerName),
                style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                color = MangroTheme.colors.textTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = visitor.productName,
                    modifier = Modifier.weight(1f),
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.textSubtitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.owner_home_quantity, visitor.quantity),
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.textSubtitle,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(
                    if (minutes > 0) {
                        R.string.owner_home_remaining
                    } else {
                        R.string.owner_home_expired
                    },
                    minutes,
                ),
                modifier = Modifier.align(Alignment.End),
                style = MangroTheme.typography.heading.headingXXS,
                color = if (minutes > 0) MangroTheme.colors.primaryNormal else MangroTheme.colors.textCanceled,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (minutes > 0) Color(0xFFFFD2B2) else MangroTheme.colors.borderDefault)
                .clickable(
                    enabled = minutes > 0,
                    onClick = { onAction(OwnerHomeAction.CompletePickup(visitor.id)) },
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_owner_check),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MangroTheme.colors.textTitle,
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = stringResource(R.string.owner_home_completed),
                style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                color = MangroTheme.colors.textTitle,
            )
        }
    }
}
