package com.swyp.mangro.feature.owner.home.component.heading

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.home.R

@Composable
internal fun SectionHeading(
    title: String,
    onViewAll: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MangroTheme.typography.heading.headingXXS,
            color = MangroTheme.colors.textTitle,
        )

        if (onViewAll != null) {
            Row(
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onViewAll,
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.owner_home_view_all),
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.textSubtitle,
                )
                Icon(
                    painter = painterResource(DesignR.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MangroTheme.colors.textSubtitle,
                )
            }
        }
    }
}
