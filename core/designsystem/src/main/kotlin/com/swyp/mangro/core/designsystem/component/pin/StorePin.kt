package com.swyp.mangro.core.designsystem.component.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroLabel
import com.swyp.mangro.core.designsystem.theme.LocalMangroColors

@Composable
fun StorePin(
    remainingCount: Int,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMangroColors.current
    val isAvailable = remainingCount > 0
    val borderColor = if (isAvailable) colors.primaryNormal else colors.borderDefault
    val iconColor = if (isAvailable) colors.primaryNormal else colors.borderDefault
    val textColor = if (isAvailable) colors.grayScale900 else colors.textSubtitle

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(borderColor)
            .padding(1.dp)
            .clip(CircleShape)
            .background(colors.surfaceNormal)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_bag),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconColor,
        )

        Text(
            text = remainingCount.toString(),
            color = textColor,
            style = ConsumerMangroLabel.labelM,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MangroStorePinPreview() {
    Row(
        modifier = Modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StorePin(remainingCount = 0)
        StorePin(remainingCount = 5)
    }
}
