package com.swyp.mangro.feature.consumer.store.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as dsR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.card.product.ProductDetail
import com.swyp.mangro.core.designsystem.component.card.product.ProductDetailCard
import com.swyp.mangro.core.designsystem.component.card.recipe.RecipeCard
import com.swyp.mangro.core.designsystem.component.image.MangroImagePageController
import com.swyp.mangro.core.designsystem.component.image.MangroImageViewer
import com.swyp.mangro.core.designsystem.theme.Gray200
import com.swyp.mangro.core.designsystem.theme.Gray900
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.consumer.store.R
import com.swyp.mangro.feature.consumer.store.wish.WishBottomSheet

private fun ProductInfo.toCardModel() = ProductDetail(
    hashtags = tags,
    name = product.name,
    category = product.category,
    remainingCount = product.remainingCount,
    originalPrice = product.originalPrice,
    discountRate = product.discountRate,
    price = product.price,
    storeName = store.name,
    address = store.address,
    distanceMeters = store.distanceMeters,
    travelTime = store.travelInfo,
    operatingHoursText = store.closingTime,
)

@Composable
fun ProductDetailScreen(
    uiState: ProductDetailUiState,
    onAction: (ProductDetailUiAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val productInfo = uiState.productInfo ?: return

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        containerColor = MangroTheme.colors.surfaceNormal,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(dsR.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.product_detail_title),
                        tint = Gray900,
                        modifier = Modifier.size(24.dp).clickable(onClick = onBackClick),
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.product_detail_title),
                        style = MangroTheme.typography.heading.headingXXS,
                        color = MangroTheme.colors.grayScale900,
                    )
                },
            )
        },
        bottomBar = {
            MangroButton(
                text = stringResource(R.string.product_detail_wish_button),
                style = MangroButtonStyle.ACTIVE,
                onClick = { onAction(ProductDetailUiAction.OnWishButtonClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        top = 20.dp,
                        bottom = 32.dp,
                        start = 20.dp,
                        end = 20.dp,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            MangroImageViewer(
                images = productInfo.images,
                controller = MangroImagePageController.Both,
            )

            ProductDetailCard(
                product = productInfo.toCardModel(),
                onStoreClick = { onAction(ProductDetailUiAction.OnStoreInfoClick) },
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Gray200),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.product_detail_recipe_section_title),
                    style = MangroTheme.typography.title.titleM,
                    color = MangroTheme.colors.textTitle,
                )

                Spacer(modifier = Modifier.height(10.dp))

                productInfo.recipes.forEach { recipe ->
                    RecipeCard(
                        recipeName = recipe.name,
                        ingredients = recipe.ingredients,
                        difficulty = recipe.difficulty,
                        onClick = { },
                    )
                }
            }

            if (uiState.isWishBottomSheetVisible) {
                WishBottomSheet(
                    product = productInfo.product,
                    wishState = uiState.wishState,
                    onQuantityChange = { onAction(ProductDetailUiAction.OnQuantityChange(it)) },
                    onDismiss = { onAction(ProductDetailUiAction.OnWishBottomSheetDismiss) },
                    onConfirm = { onAction(ProductDetailUiAction.OnWishConfirmClick) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailScreenPreview() {
    MangroTheme {
        ProductDetailScreen(
            uiState = dummyProductDetailUiState,
            onAction = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailScreenWishBottomSheetPreview() {
    MangroTheme {
        ProductDetailScreen(
            uiState = dummyProductDetailUiStateWishSheetOpen,
            onAction = {},
            onBackClick = {},
        )
    }
}
