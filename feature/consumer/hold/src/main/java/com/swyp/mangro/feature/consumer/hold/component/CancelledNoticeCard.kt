package com.swyp.mangro.feature.consumer.hold.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as dsR
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.consumer.hold.R

@Composable
fun CancelledNoticeCard(
    storeName: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MangroTheme.colors.surfaceAlter)
            .border(
                width = 1.dp,
                color = MangroTheme.colors.surfaceDisabled,
                shape = shape,
            )
            .padding(
                vertical = 34.dp,
                horizontal = 16.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(dsR.drawable.ic_store_graphic),
            contentDescription = null,
            modifier = Modifier.size(52.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.hold_cancelled_notice, storeName),
            style = MangroTheme.typography.title.titleM,
            color = MangroTheme.colors.textTitle,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(dsR.drawable.ic_error),
                contentDescription = null,
                tint = MangroTheme.colors.dangerNormal,
                modifier = Modifier.size(20.dp),
            )

            Text(
                text = stringResource(R.string.hold_cancelled_reason),
                style = MangroTheme.typography.label.labelM,
                color = MangroTheme.colors.dangerNormal,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CancelledNoticeCardPreview() {
    MangroTheme {
        CancelledNoticeCard(
            storeName = "청과마을",
            modifier = Modifier.padding(20.dp),
        )
    }
}
