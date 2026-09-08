package com.swyp.mangro.core.designsystem.component.card.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.White

@Composable
fun RecipeIngredientItemCard(
    thumbnailUrl: String,
    ingredientName: String,
    amount: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(MangroTheme.colors.surfaceAlter)
            .padding(
                horizontal = 20.dp,
                vertical = 10.dp,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MangroTheme.colors.surfaceDisabled),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = ingredientName,
            color = MangroTheme.colors.textTitle,
            style = MangroTheme.typography.body.body03,
        )

        Text(
            text = amount,
            color = MangroTheme.colors.textSubtitle,
            style = MangroTheme.typography.caption.captionS,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeIngredientItemCardPreview() {
    MangroTheme {
        Box(
            modifier = Modifier
                .background(White)
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            RecipeIngredientItemCard(
                thumbnailUrl = "",
                ingredientName = "복숭아",
                amount = "300g",
            )
        }
    }
}
