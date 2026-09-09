package com.swyp.mangro.core.designsystem.component.card.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.White

@Composable
fun RecipeStepCard(
    imageUrl: String,
    stepNumber: Int,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MangroTheme.colors.surfaceAlter),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .background(MangroTheme.colors.surfaceDisabled),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MangroTheme.colors.primaryNormal),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stepNumber.toString(),
                    color = MangroTheme.colors.textOnBrandWhite,
                    style = MangroTheme.typography.body.bodyM,
                )
            }

            Text(
                text = description,
                color = MangroTheme.colors.textTitle,
                style = MangroTheme.typography.label.labelS ?: MangroTheme.typography.label.labelM,
            )
        }
    }
}

private data class RecipeStepPreviewParam(
    val stepNumber: Int,
    val description: String,
)

private class RecipeStepCardPreviewParamProvider : PreviewParameterProvider<RecipeStepPreviewParam> {
    override val values = sequenceOf(
        RecipeStepPreviewParam(1, "복숭아를 한 입 크기로 썬다"),
        RecipeStepPreviewParam(2, "한 입에 와앙 먹는다"),
        RecipeStepPreviewParam(3, "배부르다"),
    )
}

@Preview(showBackground = true)
@Composable
private fun RecipeStepCardPreview(
    @PreviewParameter(RecipeStepCardPreviewParamProvider::class) param: RecipeStepPreviewParam,
) {
    MangroTheme {
        Box(
            modifier = Modifier
                .background(White)
                .padding(20.dp),
        ) {
            RecipeStepCard(
                imageUrl = "",
                stepNumber = param.stepNumber,
                description = param.description,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
