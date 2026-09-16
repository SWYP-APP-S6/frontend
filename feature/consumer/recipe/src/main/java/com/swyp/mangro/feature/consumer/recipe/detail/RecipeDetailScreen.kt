package com.swyp.mangro.feature.consumer.recipe.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swyp.mangro.core.designsystem.R as dsR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.card.recipe.RecipeHeaderCard
import com.swyp.mangro.core.designsystem.component.card.recipe.RecipeStepCard
import com.swyp.mangro.core.designsystem.component.label.MangroOutlinedLabel
import com.swyp.mangro.core.designsystem.theme.Gray900
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.consumer.recipe.R

@Composable
fun RecipeDetailScreen(
    uiState: RecipeDetailUiState,
    onAction: (RecipeDetailUiAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detail = uiState.detail ?: return

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MangroTheme.colors.surfaceNormal,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(dsR.drawable.ic_arrow_left),
                        contentDescription = null,
                        tint = Gray900,
                        modifier = Modifier.size(24.dp).clickable(onClick = onBackClick),
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.recipe_detail_title),
                        style = MangroTheme.typography.heading.headingXXS,
                        color = MangroTheme.colors.grayScale900,
                    )
                },
            )
        },
        bottomBar = {
            MangroButton(
                text = stringResource(R.string.recipe_detail_wish_button),
                style = MangroButtonStyle.ACTIVE,
                onClick = { onAction(RecipeDetailUiAction.OnWishClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            AsyncImage(
                model = detail.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MangroTheme.colors.surfaceAlter)
                    .height(454.dp),
            )

            RecipeHeaderCard(
                calorie = detail.calorie,
                recipeName = detail.name,
                cookingMinutes = detail.cookingMinutes,
                difficulty = detail.difficulty,
            )

            RecipeInfoSection(
                label = stringResource(R.string.recipe_detail_ingredients_title),
                content = {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        detail.ingredients.forEach { ingredient ->
                            MangroOutlinedLabel(content = ingredient)
                        }
                    }
                },
            )

            RecipeInfoSection(
                label = stringResource(R.string.recipe_detail_nutrition_title),
                description = stringResource(R.string.recipe_detail_nutrition_basis),
                content = {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        detail.nutritionLabels.forEach { label ->
                            MangroOutlinedLabel(content = label)
                        }
                    }
                },
            )

            RecipeInfoSection(
                label = stringResource(R.string.recipe_detail_steps_title),
                content = {
                    detail.steps.forEach { step ->
                        RecipeStepCard(
                            imageUrl = step.imageUrl,
                            stepNumber = step.stepNumber,
                            description = step.description,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                },
            )
        }
    }
}

@Composable
private fun RecipeInfoSection(
    label: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 4.dp,
            color = MangroTheme.colors.surfaceAlter,
        )

        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                top = 24.dp,
                bottom = 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MangroTheme.typography.label.labelL,
                color = MangroTheme.colors.textTitle,
            )

            description?.let {
                Text(
                    text = description,
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.textTitle,
                )
            }
        }

        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeDetailScreenPreview() {
    MangroTheme {
        RecipeDetailScreen(uiState = dummyRecipeDetailUiState, onAction = {}, onBackClick = {})
    }
}
