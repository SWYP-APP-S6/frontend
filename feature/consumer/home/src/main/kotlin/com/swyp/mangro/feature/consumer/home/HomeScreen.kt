package com.swyp.mangro.feature.consumer.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.NaverMap
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.banner.ActionBanner
import com.swyp.mangro.core.designsystem.component.card.map.MapStoreCard
import com.swyp.mangro.core.designsystem.component.card.map.StoreProduct
import com.swyp.mangro.core.designsystem.component.tab.MangroPillTabItem
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import kotlinx.collections.immutable.toPersistentList

@OptIn(ExperimentalNaverMapApi::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(com.swyp.mangro.core.designsystem.R.drawable.ic_location),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp),
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = uiState.locationName,
                            style = MangroTheme.typography.title.titleM,
                            color = MangroTheme.colors.grayScale900,
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MangroTheme.colors.surfaceAlter)
                            .padding(4.dp)
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MangroPillTabItem(
                            text = stringResource(R.string.home_view_mode_map),
                            isSelected = uiState.viewMode == HomeViewMode.MAP,
                            onClick = { onAction(HomeUiAction.ViewModeChanged(HomeViewMode.MAP)) },
                        )

                        MangroPillTabItem(
                            text = stringResource(R.string.home_view_mode_list),
                            isSelected = uiState.viewMode == HomeViewMode.LIST,
                            onClick = { onAction(HomeUiAction.ViewModeChanged(HomeViewMode.LIST)) },
                        )
                    }
                },
            )
        },
        bottomBar = {
            ConsumerBottomAppBar(
                menus = ConsumerMenu.entries.toPersistentList(),
                currentMenu = ConsumerMenu.HOME,
                onMenuClick = { onAction(HomeUiAction.BottomMenuClicked(it)) },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(innerPadding),
        ) {
            when (uiState.viewMode) {
                HomeViewMode.MAP -> {
                    NaverMap(modifier = Modifier.fillMaxSize())
                }
                HomeViewMode.LIST -> {}
            }

            if (!uiState.isLocationPermissionGranted) {
                ActionBanner(
                    iconRes = com.swyp.mangro.core.designsystem.R.drawable.ic_error,
                    stringRes = com.swyp.mangro.core.designsystem.R.string.banner_location_permission,
                    modifier = Modifier
                        .align(Alignment.TopCenter),
                    action = {
                        Text(
                            text = stringResource(com.swyp.mangro.core.designsystem.R.string.banner_action_turn_on),
                            color = MangroTheme.colors.primaryNormal,
                            style = MangroTheme.typography.label.labelM,
                            modifier = Modifier.clickable {
                                onAction(HomeUiAction.PermissionBannerActionClicked)
                            },
                        )
                    },
                )
            }

            uiState.selectedStore?.let { store ->
                MapStoreCard(
                    storeName = store.storeName,
                    closingTime = store.closingTime,
                    products = store.products,
                    onProductClick = { onAction(HomeUiAction.ProductClicked(it)) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    travelInfo = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = ImageVector.vectorResource(com.swyp.mangro.core.designsystem.R.drawable.ic_directions_walk),
                                contentDescription = null,
                            )
                            Text(
                                text = stringResource(
                                    R.string.home_walking_minutes,
                                    store.walkingMinutes,
                                ),
                                style = MangroTheme.typography.caption.captionS,
                            )
                        }
                    },
                )
            }
        }
    }
}

private val previewStoreProducts = listOf(
    StoreProduct(id = "1", imageUrl = "", discountRate = 60, productName = "복숭아 4입", price = 4_000),
    StoreProduct(id = "2", imageUrl = "", discountRate = 50, productName = "알배기 배추 2통", price = 3_000),
    StoreProduct(id = "3", imageUrl = "", discountRate = null, productName = "대파 1단", price = 3_500),
)

private class HomeUiStatePreviewProvider : PreviewParameterProvider<HomeUiState> {
    override val values: Sequence<HomeUiState>
        get() = sequenceOf(
            HomeUiState(
                locationName = "망원동",
            ),
            HomeUiState(
                locationName = "망원동",
                isLocationPermissionGranted = false,
            ),
            HomeUiState(
                locationName = "망원동",
                selectedStore = SelectedStoreDetail(
                    storeId = "store1",
                    storeName = "청과마을",
                    closingTime = "19:30",
                    walkingMinutes = 7,
                    products = previewStoreProducts,
                ),
            ),
        )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview(
    @PreviewParameter(HomeUiStatePreviewProvider::class) uiState: HomeUiState,
) {
    MangroTheme {
        HomeScreen(
            uiState = uiState,
            onAction = {},
        )
    }
}
