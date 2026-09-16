package com.swyp.mangro.feature.consumer.hold.detail

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishDetailCard
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishStatus
import com.swyp.mangro.core.designsystem.component.image.MangroImagePageController
import com.swyp.mangro.core.designsystem.component.image.MangroImageViewer
import com.swyp.mangro.core.designsystem.theme.Gray900
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.consumer.hold.R

@Composable
fun HoldDetailScreen(
    uiState: HoldDetailUiState,
    onAction: (HoldDetailUiAction) -> Unit,
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
                        text = stringResource(R.string.hold_detail_title),
                        style = MangroTheme.typography.heading.headingXXS,
                        color = MangroTheme.colors.grayScale900,
                    )
                },
            )
        },
        bottomBar = {
            val isInProgress = detail.item.status == WishStatus.IN_PROGRESS

            MangroButton(
                text = stringResource(R.string.hold_detail_cancel_button),
                style = if (isInProgress) MangroButtonStyle.ACTIVE else MangroButtonStyle.DEFAULT,
                onClick = {
                    if (isInProgress) {
                        onAction(HoldDetailUiAction.OnCancelClick)
                    }
                },
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
            MangroImageViewer(
                images = detail.images,
                controller = MangroImagePageController.Both,
            )

            WishDetailCard(
                item = detail.item,
                modifier = Modifier.padding(
                    vertical = 16.dp,
                    horizontal = 20.dp,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            HoldDetailInfoRow(
                label = stringResource(R.string.hold_detail_store_label),
                mainValue = detail.storeName,
            )

            HoldDetailInfoRow(
                label = stringResource(R.string.hold_detail_requested_at_label),
                mainValue = detail.requestedDateText,
                subValue = detail.requestedTimeText,
            )

            HoldDetailInfoRow(
                label = stringResource(R.string.hold_detail_expired_at_label),
                mainValue = detail.expiredDateText,
                subValue = detail.expiredTimeText,
            )
        }
    }
}

@Composable
private fun HoldDetailInfoRow(
    label: String,
    mainValue: String,
    modifier: Modifier = Modifier,
    subValue: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 12.dp,
                horizontal = 20.dp,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MangroTheme.typography.label.labelM,
            color = MangroTheme.colors.textBody,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = mainValue,
                style = MangroTheme.typography.body.bodyM,
                color = MangroTheme.colors.textSubtitle,
            )

            subValue?.let {
                Text(
                    text = subValue,
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.textSubtitle,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HoldDetailScreenInProgressPreview() {
    MangroTheme {
        HoldDetailScreen(uiState = dummyHoldDetailUiState, onAction = {}, onBackClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HoldDetailScreenPastPreview() {
    MangroTheme {
        HoldDetailScreen(uiState = dummyHoldDetailUiStatePast, onAction = {}, onBackClick = {})
    }
}
