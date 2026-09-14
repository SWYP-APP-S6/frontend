package com.swyp.mangro.feature.owner.product.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
internal fun OwnerProductLabel(
    text: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    isRequired: Boolean = false,
    textStyle: TextStyle = MangroTheme.typography.title.titleM,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (isRequired) 2.dp else 4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = text,
                style = textStyle,
                color = MangroTheme.colors.textTitle,
            )
            if (isRequired) Icon(painterResource(DesignR.drawable.ic_star), null, tint = Color.Unspecified)
        }
        if (hint != null) {
            Text(
                text = hint,
                style = MangroTheme.typography.caption.captionS,
                color = MangroTheme.colors.textSubtitle,
            )
        }
    }
}
