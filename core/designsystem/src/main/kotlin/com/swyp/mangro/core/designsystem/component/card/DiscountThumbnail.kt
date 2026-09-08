package com.swyp.mangro.core.designsystem.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swyp.mangro.core.designsystem.component.badge.MangroBadge
import com.swyp.mangro.core.designsystem.theme.ConsumerMangroCaption
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun DiscountThumbnail(
    imageUrl: String,
    discountRate: Int?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)

    Box(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(MangroTheme.colors.surfaceDisabled),
        )

        if (discountRate != null) {
            MangroBadge(
                text = "$discountRate%",
                containerColor = MangroTheme.colors.primaryNormal,
                contentColor = MangroTheme.colors.textOnBrandWhite,
                textStyle = ConsumerMangroCaption.captionS,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
            )
        }
    }
}
