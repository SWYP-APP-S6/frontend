package com.swyp.mangro.feature.owner.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
internal fun OwnerOnboardingField(
    label: String,
    hint: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(
            modifier = Modifier.padding(bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MangroTheme.typography.heading.headingXXS,
                    color = MangroTheme.colors.grayScale900,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_star),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }

            if (hint != null) {
                Text(
                    text = hint,
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.textSubtitle,
                )
            }
        }
        content()
    }
}
