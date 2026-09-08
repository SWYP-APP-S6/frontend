package com.swyp.mangro.core.designsystem.component.card.recipe

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.label.MangroLabel
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.Orange700

enum class RecipeDifficulty {
    LOW,
    MEDIUM,
    HIGH,
}

private data class RecipeDifficultyStyle(
    @param:StringRes val labelTextRes: Int,
    val containerColor: Color,
    val contentColor: Color,
)

@Composable
private fun RecipeDifficulty.toStyle(): RecipeDifficultyStyle = when (this) {
    RecipeDifficulty.LOW -> RecipeDifficultyStyle(
        labelTextRes = R.string.recipe_difficulty_low,
        containerColor = MangroTheme.colors.surfaceDisabled,
        contentColor = MangroTheme.colors.textSubtitle,
    )
    RecipeDifficulty.MEDIUM -> RecipeDifficultyStyle(
        labelTextRes = R.string.recipe_difficulty_medium,
        containerColor = MangroTheme.colors.primaryLight,
        contentColor = MangroTheme.colors.nutsNormal,
    )
    RecipeDifficulty.HIGH -> RecipeDifficultyStyle(
        labelTextRes = R.string.recipe_difficulty_high,
        containerColor = Orange700,
        contentColor = MangroTheme.colors.textOnBrandWhite,
    )
}

@Composable
fun RecipeCard(
    recipeName: String,
    ingredients: List<String>,
    difficulty: RecipeDifficulty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                vertical = 16.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val style = difficulty.toStyle()
                MangroLabel(
                    content = stringResource(style.labelTextRes),
                    contentColor = style.contentColor,
                    containerColor = style.containerColor,
                )

                Text(
                    text = recipeName,
                    color = MangroTheme.colors.textTitle,
                    style = MangroTheme.typography.body.bodyM,
                )
            }

            Text(
                text = ingredients.joinToString(separator = " · "),
                color = MangroTheme.colors.textBody,
                style = MangroTheme.typography.body.body03,
            )
        }

        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier
                .size(24.dp),
        )
    }
}

private data class RecipeCardPreviewParam(
    val recipeName: String,
    val ingredients: List<String>,
    val difficulty: RecipeDifficulty,
)

private class RecipeCardPreviewParamProvider : PreviewParameterProvider<RecipeCardPreviewParam> {
    override val values = sequenceOf(
        RecipeCardPreviewParam(
            recipeName = "복숭아 샐러드",
            ingredients = listOf("복숭아", "요거트", "견과류"),
            difficulty = RecipeDifficulty.LOW,
        ),
        RecipeCardPreviewParam(
            recipeName = "복숭아 얼그레이 샐러드",
            ingredients = listOf("복숭아", "요거트", "견과류", "얼그레이"),
            difficulty = RecipeDifficulty.MEDIUM,
        ),
        RecipeCardPreviewParam(
            recipeName = "복숭아 마스카포네치즈 카나페",
            ingredients = listOf("복숭아", "요거트", "견과류", "마스카포네치즈"),
            difficulty = RecipeDifficulty.HIGH,
        ),
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RecipeCardPreview(
    @PreviewParameter(RecipeCardPreviewParamProvider::class) param: RecipeCardPreviewParam,
) {
    MangroTheme {
        RecipeCard(
            recipeName = param.recipeName,
            ingredients = param.ingredients,
            difficulty = param.difficulty,
            onClick = {},
        )
    }
}
