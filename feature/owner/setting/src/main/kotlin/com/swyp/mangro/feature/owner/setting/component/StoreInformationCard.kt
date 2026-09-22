package com.swyp.mangro.feature.owner.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.setting.R

@Composable
internal fun StoreInformationCard(
    storeName: String,
    storePhone: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MangroTheme.colors.surfaceNormal, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_store_front),
                contentDescription = null,
                tint = MangroTheme.colors.primaryNormal,
                modifier = Modifier
                    .background(MangroTheme.colors.primaryLight, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .size(24.dp),
            )
            StoreInformationField(
                label = stringResource(R.string.owner_setting_store_name),
                value = storeName,
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = MangroTheme.colors.borderSubtle)
        StoreInformationField(
            label = stringResource(R.string.owner_setting_store_phone),
            value = storePhone,
        )
    }
}

@Composable
private fun StoreInformationField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MangroTheme.typography.caption.captionS,
            color = MangroTheme.colors.textSubtitle,
        )

        Text(
            text = value.ifBlank { stringResource(R.string.owner_setting_unregistered) },
            style = MangroTheme.typography.title.titleM,
            color = if (value.isBlank()) MangroTheme.colors.textCanceled else MangroTheme.colors.textTitle,
        )
    }
}
