package com.swyp.mangro.feature.consumer.hold.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as dsR
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.White
import com.swyp.mangro.feature.consumer.hold.R

@Composable
fun StoreInfoCard(
    storeName: String,
    address: String,
    phoneNumber: String,
    operatingStatusText: String,
    onCopyAddressClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(White)
            .padding(
                top = 20.dp,
                bottom = 16.dp,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(dsR.drawable.ic_store_front),
                contentDescription = null,
                tint = MangroTheme.colors.textTitle,
            )

            Text(
                text = storeName,
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.label.labelL,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        StoreInfoRow(
            iconRes = dsR.drawable.ic_location_on,
            content = address,
            actionText = stringResource(R.string.store_info_card_copy_address),
            actionIconRes = dsR.drawable.ic_copy,
            onActionClick = onCopyAddressClick,
        )

        StoreInfoRow(
            iconRes = dsR.drawable.ic_call,
            content = phoneNumber,
            actionText = stringResource(R.string.store_info_card_call),
            onActionClick = onCallClick,
        )

        StoreInfoRow(
            iconRes = dsR.drawable.ic_alarm_on_16px,
            content = operatingStatusText,
        )
    }
}

@Composable
private fun StoreInfoRow(
    iconRes: Int,
    content: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    actionIconRes: Int? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MangroTheme.colors.grayScale700,
            )

            Text(
                text = content,
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.label.labelM,
            )
        }

        if (actionText != null && onActionClick != null) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onActionClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                actionIconRes?.let {
                    Icon(
                        imageVector = ImageVector.vectorResource(it),
                        contentDescription = null,
                        tint = MangroTheme.colors.primaryNormal,
                    )
                }

                Text(
                    text = actionText,
                    color = MangroTheme.colors.primaryNormal,
                    style = MangroTheme.typography.label.labelM,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StoreInfoCardPreview() {
    MangroTheme {
        StoreInfoCard(
            storeName = "청과마을",
            address = "서울 마포구 망원로 12",
            phoneNumber = "02-5894-1982",
            operatingStatusText = "영업중 · 10:00~21:00",
            onCopyAddressClick = {},
            onCallClick = {},
        )
    }
}
