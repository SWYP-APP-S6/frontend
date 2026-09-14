package com.swyp.mangro.core.designsystem.component.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.banner.NoticeBanner
import com.swyp.mangro.core.designsystem.component.card.product.Product
import com.swyp.mangro.core.designsystem.component.card.product.ProductCategory
import com.swyp.mangro.core.designsystem.component.card.product.ProductListCard
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepper
import com.swyp.mangro.core.designsystem.component.stepper.MangroStepperSize
import com.swyp.mangro.core.designsystem.theme.Black
import com.swyp.mangro.core.designsystem.theme.MangroTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangroBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    showScrim: Boolean = true,
    bottomBar: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = Dp.Unspecified,
        shape = RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 32.dp,
        ),
        containerColor = MangroTheme.colors.textOnBrandWhite,
        scrimColor = if (showScrim) Black.copy(alpha = 0.3f) else Color.Transparent,
        dragHandle = { CustomDragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(
                        weight = 1f,
                        fill = false,
                    ),
                content = content,
            )

            bottomBar()
        }
    }
}

@Composable
private fun CustomDragHandle(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(
                vertical = 16.dp,
            )
            .size(
                width = 60.dp,
                height = 4.dp,
            )
            .background(
                color = MangroTheme.colors.borderDefault,
                shape = RoundedCornerShape(100.dp),
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun MangroBottomSheetPreview() {
    MangroTheme {
        var showSheet by remember { mutableStateOf(true) }
        var quantity by remember { mutableIntStateOf(1) }

        val shape = RoundedCornerShape(10.dp)
        val dummyProduct = Product(
            id = "1",
            imageUrl = "",
            discountRate = 60,
            name = "복숭아 4입",
            price = 4_000,
            originalPrice = 10_000,
            category = ProductCategory.VEGETABLES,
            remainingCount = 3,
        )

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Button(onClick = { showSheet = true }) {
                Text(text = "바텀시트 열기")
            }

            if (showSheet) {
                MangroBottomSheet(
                    onDismissRequest = { showSheet = false },
                    bottomBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 20.dp,
                                    bottom = 32.dp,
                                    start = 20.dp,
                                    end = 20.dp,
                                ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            MangroButton(
                                text = "수정하기",
                                onClick = {},
                                style = MangroButtonStyle.DEFAULT,
                            )

                            MangroButton(
                                text = "15분 간 찜하기",
                                onClick = {},
                                style = MangroButtonStyle.ACTIVE,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 4.dp,
                                horizontal = 20.dp,
                            ),
                    ) {
                        Text(
                            text = "이 상품 찜하기",
                            color = MangroTheme.colors.textTitle,
                            style = MangroTheme.typography.heading.headingXXS,
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "아래 버튼을 누르면 15분 동안 상품을 찜할 수 있어요.",
                            color = MangroTheme.colors.textSubtitle,
                            style = MangroTheme.typography.caption.captionS,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        ProductListCard(
                            product = dummyProduct,
                            modifier = Modifier
                                .clip(shape)
                                .background(MangroTheme.colors.surfaceNormal)
                                .border(
                                    width = 1.dp,
                                    color = MangroTheme.colors.borderDefault,
                                    shape = shape,
                                )
                                .padding(12.dp),
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    bottom = 16.dp,
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "찜할 수량",
                                color = MangroTheme.colors.textTitle,
                                style = MangroTheme.typography.title.titleM,
                            )

                            MangroStepper(
                                value = quantity,
                                onValueChange = { quantity = it },
                                size = MangroStepperSize.SMALL,
                                minValue = 1,
                                maxValue = dummyProduct.remainingCount,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    bottom = 2.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "총 금액",
                                color = MangroTheme.colors.textTitle,
                                style = MangroTheme.typography.heading.headingXXS,
                            )

                            Text(
                                text = "4000원",
                                color = MangroTheme.colors.textTitle,
                                style = MangroTheme.typography.heading.headingM,
                            )
                        }

                        Text(
                            text = "4000원  *  1개",
                            color = MangroTheme.colors.textBody,
                            style = MangroTheme.typography.body.body03,
                            modifier = Modifier.align(Alignment.End),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        NoticeBanner(
                            text = "15분 안에 매장에서 픽업하지 않으시면\n찜이 자동으로 취소돼요.",
                        )
                    }
                }
            }
        }
    }
}
