package com.swyp.mangro.feature.consumer.hold.complete

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.R as dsR
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.card.purchase.PurchaseInfoCard
import com.swyp.mangro.core.designsystem.theme.Gray900
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.consumer.hold.R
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PickupCompleteScreen(
    uiState: PickupCompleteUiState,
    onAction: (PickupCompleteUiAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val info = uiState.info ?: return

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MangroTheme.colors.surfaceNormal,
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFD2B2)),
                navigationIcon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(dsR.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.pickup_complete_title),
                        tint = Gray900,
                        modifier = Modifier.size(24.dp).clickable(onClick = onBackClick),
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.pickup_complete_title),
                        style = MangroTheme.typography.heading.headingXXS,
                        color = MangroTheme.colors.grayScale900,
                    )
                },
            )
        },
        bottomBar = {
            ConsumerBottomAppBar(
                menus = persistentListOf(ConsumerMenu.HOME, ConsumerMenu.WISH_LIST, ConsumerMenu.MY),
                currentMenu = ConsumerMenu.WISH_LIST,
                onMenuClick = { menu -> onAction(PickupCompleteUiAction.OnMenuClick(menu)) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0888f to Color(0xFFFFD2B2),
                                0.905f to MangroTheme.colors.surfaceNormal,
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, Float.POSITIVE_INFINITY),
                        ),
                    )
                    .padding(
                        top = 32.dp,
                        bottom = 64.dp,
                        start = 20.dp,
                        end = 20.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(dsR.drawable.ic_clap),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.pickup_complete_message),
                    style = MangroTheme.typography.heading.headingS,
                    color = MangroTheme.colors.primaryNormal,
                )

                Spacer(modifier = Modifier.height(40.dp))

                PurchaseInfoCard(
                    receipt = info.purchaseInfo,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PickupCompleteScreenPreview() {
    MangroTheme {
        PickupCompleteScreen(uiState = dummyPickupCompleteUiState, onAction = {}, onBackClick = {})
    }
}
