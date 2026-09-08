package com.swyp.mangro.core.designsystem.component.banner

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.White

@Composable
fun ActionBanner(
    @DrawableRes iconRes: Int,
    @StringRes stringRes: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    action: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MangroTheme.colors.primaryLight)
            .padding(
                horizontal = 20.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = null,
            tint = MangroTheme.colors.primaryNormal,
            modifier = Modifier.size(iconSize),
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = stringResource(stringRes),
            color = MangroTheme.colors.textTitle,
            style = MangroTheme.typography.caption.captionM ?: MangroTheme.typography.caption.captionS,
            modifier = Modifier
                .weight(1f),
        )

        action()
    }
}

@Preview
@Composable
private fun ActionBannerPreview() {
    MangroTheme {
        Column(
            modifier = Modifier
                .background(White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ActionBanner(
                iconRes = R.drawable.ic_error,
                stringRes = R.string.banner_location_permission,
                action = {
                    Text(
                        text = stringResource(R.string.banner_action_turn_on),
                        color = MangroTheme.colors.primaryNormal,
                        style = MangroTheme.typography.label.labelM,
                        modifier = Modifier.clickable {},
                    )
                },
            )

            ActionBanner(
                iconRes = R.drawable.ic_alert,
                iconSize = 24.dp,
                stringRes = R.string.banner_new_like,
                action = {
                    Text(
                        text = stringResource(R.string.banner_action_confirm),
                        color = MangroTheme.colors.primaryNormal,
                        style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.label.labelM,
                        modifier = Modifier.clickable {},
                    )
                },
            )
        }
    }
}
