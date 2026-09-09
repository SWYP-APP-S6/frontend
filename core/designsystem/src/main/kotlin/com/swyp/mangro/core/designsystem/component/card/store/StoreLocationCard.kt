package com.swyp.mangro.core.designsystem.component.card.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun StoreLocationCard(
    imageUrl: String,
    onDirectionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = 0.6.dp,
                color = MangroTheme.colors.borderDefault,
                shape = shape,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MangroTheme.colors.surfaceAlter),
            )

            Icon(
                painter = painterResource(R.drawable.ic_location_graphic),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MangroTheme.colors.surfaceNormal)
                .clickable(onClick = onDirectionsClick)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.store_location_card_button),
                color = MangroTheme.colors.textBody,
                style = MangroTheme.typography.body.bodyM,
            )

            Spacer(modifier = Modifier.width(2.dp))

            Icon(
                painter = painterResource(R.drawable.ic_exit),
                contentDescription = null,
                tint = MangroTheme.colors.grayScale700,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StoreDirectionsCardPreview() {
    MangroTheme {
        Box(
            modifier = Modifier
                .padding(20.dp),
        ) {
            StoreLocationCard(
                imageUrl = "",
                onDirectionsClick = {},
            )
        }
    }
}
