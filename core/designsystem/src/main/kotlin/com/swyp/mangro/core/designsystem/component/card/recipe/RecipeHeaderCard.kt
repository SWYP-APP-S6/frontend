package com.swyp.mangro.core.designsystem.component.card.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.label.MangroLabel
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@Composable
fun RecipeHeaderCard(
    calorie: Int,
    recipeName: String,
    cookingMinutes: Int,
    difficulty: RecipeDifficulty,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 24.dp,
                horizontal = 20.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        MangroLabel(
            content = stringResource(R.string.recipe_calorie, calorie),
            contentColor = MangroTheme.colors.textBody,
            containerColor = MangroTheme.colors.surfaceAlter,
            shape = RoundedCornerShape(50),
        )

        Text(
            text = recipeName,
            color = MangroTheme.colors.textTitle,
            style = MangroTheme.typography.title.titleM,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MangroLabel(
                content = stringResource(R.string.recipe_cooking_time, cookingMinutes),
                contentColor = MangroTheme.colors.primaryNormal,
                containerColor = MangroTheme.colors.warningBg,
            )

            val style = difficulty.toStyle()
            MangroLabel(
                content = stringResource(style.labelTextRes),
                contentColor = style.contentColor,
                containerColor = style.containerColor,
            )
        }
    }
}

private data class RecipeHeaderPreviewParam(
    val calorie: Int,
    val cookingMinutes: Int,
    val difficulty: RecipeDifficulty,
)

private class RecipeHeaderCardPreviewParamProvider : PreviewParameterProvider<RecipeHeaderPreviewParam> {
    override val values = sequenceOf(
        RecipeHeaderPreviewParam(calorie = 167, cookingMinutes = 20, difficulty = RecipeDifficulty.LOW),
        RecipeHeaderPreviewParam(calorie = 320, cookingMinutes = 35, difficulty = RecipeDifficulty.MEDIUM),
        RecipeHeaderPreviewParam(calorie = 540, cookingMinutes = 60, difficulty = RecipeDifficulty.HIGH),
    )
}

@Preview(showBackground = true)
@Composable
private fun RecipeHeaderCardPreview(
    @PreviewParameter(RecipeHeaderCardPreviewParamProvider::class) param: RecipeHeaderPreviewParam,
) {
    MangroTheme {
        RecipeHeaderCard(
            calorie = param.calorie,
            recipeName = "복숭아 샐러드",
            cookingMinutes = param.cookingMinutes,
            difficulty = param.difficulty,
        )
    }
}
