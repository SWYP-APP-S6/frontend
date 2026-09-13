package com.swyp.mangro.feature.owner.product.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
internal fun OwnerProductLabel(
    text: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = text,
            style = MangroTheme.typography.title.titleM,
            color = MangroTheme.colors.textTitle,
        )
        if (hint != null) {
            Text(
                text = hint,
                style = MangroTheme.typography.caption.captionS,
                color = MangroTheme.colors.textSubtitle,
            )
        }
    }
}
