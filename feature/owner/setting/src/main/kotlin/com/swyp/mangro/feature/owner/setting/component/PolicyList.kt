package com.swyp.mangro.feature.owner.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy

@Composable
internal fun PolicyList(
    onPolicyClick: (OwnerPolicy) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MangroTheme.colors.surfaceNormal),
    ) {
        OwnerPolicy.entries.forEachIndexed { index, policy ->
            if (index > 0) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MangroTheme.colors.borderSubtle)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = { onPolicyClick(policy) })
                    .heightIn(min = 64.dp)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(policy.titleRes),
                    modifier = Modifier.weight(1f),
                    style = MangroTheme.typography.body.bodyM,
                    color = MangroTheme.colors.textTitle,
                )
                Icon(
                    painter = painterResource(DesignR.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = MangroTheme.colors.textSubtitle,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
