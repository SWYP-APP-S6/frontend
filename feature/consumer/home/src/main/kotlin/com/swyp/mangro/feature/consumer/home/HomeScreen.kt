package com.swyp.mangro.feature.consumer.home

import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapEffect
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.MarkerComposable
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.overlay.OverlayImage
import com.swyp.mangro.core.designsystem.R
import com.swyp.mangro.core.designsystem.component.MangroStorePin
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.ConsumerMenu
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.banner.ActionBanner
import com.swyp.mangro.core.designsystem.component.card.map.MapStoreCard
import com.swyp.mangro.core.designsystem.component.card.map.StoreProduct
import com.swyp.mangro.core.designsystem.component.card.product.ProductListCard
import com.swyp.mangro.core.designsystem.component.card.product.toLabelTextRes
import com.swyp.mangro.core.designsystem.component.count
import com.swyp.mangro.core.designsystem.component.storePinStateOf
import com.swyp.mangro.core.designsystem.component.tab.MangroPillTabItem
import com.swyp.mangro.core.designsystem.theme.Gray50
import com.swyp.mangro.core.designsystem.theme.Gray900
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.core.designsystem.theme.White
import com.swyp.mangro.core.model.product.ProductCategory
import com.swyp.mangro.feature.consumer.home.HomeViewMode.LIST
import com.swyp.mangro.feature.consumer.home.R as homeR
import com.swyp.mangro.feature.consumer.home.component.CountdownCard
import com.swyp.mangro.feature.consumer.home.component.HomeCategoryChip
import com.swyp.mangro.feature.consumer.home.component.SortDropdown
import com.swyp.mangro.feature.consumer.home.component.StoreGroupHeader
import kotlin.collections.filter
import kotlin.math.roundToInt
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
            HomeTopBar(
                locationName = uiState.locationName,
                viewMode = uiState.viewMode,
                onViewModeChanged = { onAction(HomeUiAction.ViewModeChanged(it)) },
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
                .padding(innerPadding),
        ) {
            when (uiState.viewMode) {
                HomeViewMode.MAP -> HomeMapContent(uiState = uiState, onAction = onAction)
                LIST -> HomeListContent(uiState = uiState, onAction = onAction)
            }

            HomeTopOverlay(
                uiState = uiState,
                onAction = onAction,
                modifier = Modifier.align(Alignment.TopCenter),
            )

            SelectedStoreCard(
                selectedStore = uiState.selectedStore,
                onAction = onAction,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    locationName: String,
    viewMode: HomeViewMode,
    onViewModeChanged: (HomeViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    MangroDefaultStartAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_location),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = locationName,
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
                    text = stringResource(homeR.string.home_view_mode_map),
                    isSelected = viewMode == HomeViewMode.MAP,
                    onClick = { onViewModeChanged(HomeViewMode.MAP) },
                )
                MangroPillTabItem(
                    text = stringResource(homeR.string.home_view_mode_list),
                    isSelected = viewMode == LIST,
                    onClick = { onViewModeChanged(LIST) },
                )
            }
        },
    )
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
private fun HomeMapContent(
    uiState: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
//    if (uiState.storePins.isEmpty()) {
//        LocationPermissionRequiredContent(
//            onExpandRadiusClick = { onAction(HomeUiAction.ExpandRadiusClicked) },
//            modifier = Modifier.fillMaxSize(),
//        )
//        return
//    }

    val cameraPositionState = rememberCameraPositionState()
    var selectedPinScreenOffset by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(uiState.locationLatitude, uiState.locationLongitude) {
        val lat = uiState.locationLatitude
        val lng = uiState.locationLongitude
        if (lat != null && lng != null) {
            cameraPositionState.position = CameraPosition(LatLng(lat, lng), 15.0)
        }
    }

    val context = LocalContext.current
    val locationSource = rememberFusedLocationSource()

    NaverMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        locationSource = locationSource,
        properties = MapProperties(locationTrackingMode = LocationTrackingMode.Follow),
        uiSettings = MapUiSettings(isLocationButtonEnabled = true),
    ) {
        uiState.storePins.forEach { pin ->
            val isSelected = uiState.selectedStore?.storeId == pin.storeId
            MarkerComposable(
                pin.storeId,
                isSelected,
                state = rememberSaveable(saver = MarkerState.Saver) {
                    MarkerState(position = LatLng(pin.latitude, pin.longitude))
                },
                onClick = {
                    onAction(HomeUiAction.StorePinClicked(pin.storeId))
                    true
                },
            ) {
                MangroStorePin(
                    state = storePinStateOf(count = pin.pinState.count, name = "", isSelected = false),
                    onTap = { onAction(HomeUiAction.StorePinClicked(pin.storeId)) },
                    modifier = if (isSelected) Modifier.size(1.dp).alpha(0f) else Modifier,
                )
            }
        }

        val selectedPin = uiState.selectedStore?.let { detail ->
            uiState.storePins.find { it.storeId == detail.storeId }
        }

        MapEffect(selectedPin) { map ->
            selectedPinScreenOffset = selectedPin?.let {
                val point = map.projection.toScreenLocation(LatLng(it.latitude, it.longitude))
                Offset(point.x, point.y)
            }
        }

        val primaryColor = MangroTheme.colors.primaryNormal

        MapEffect(primaryColor) { map ->
            val colorInt = primaryColor.toArgb()
            val bitmap = createBitmap(48, 48).apply {
                val canvas = Canvas(this)
                val fillPaint = Paint().apply {
                    color = colorInt
                    isAntiAlias = true
                }
                val strokePaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                }
                canvas.drawCircle(24f, 24f, 20f, fillPaint)
                canvas.drawCircle(24f, 24f, 20f, strokePaint)
            }

            map.locationOverlay.apply {
                icon = OverlayImage.fromBitmap(bitmap)
                circleColor = primaryColor.copy(alpha = 0.2f).toArgb()
                circleOutlineColor = colorInt
                circleOutlineWidth = 2
            }
        }

        MapEffect(cameraPositionState.isMoving) { map ->
            if (!cameraPositionState.isMoving) {
                val bounds = map.contentBounds
                onAction(
                    HomeUiAction.MapBoundsChanged(
                        minLat = bounds.southLatitude,
                        maxLat = bounds.northLatitude,
                        minLng = bounds.westLongitude,
                        maxLng = bounds.eastLongitude,
                    ),
                )
            }
        }
    }

    val selectedDetail = uiState.selectedStore
    val offset = selectedPinScreenOffset
    if (selectedDetail != null && offset != null) {
        var pinSize by remember { mutableStateOf(IntSize.Zero) }

        MangroStorePin(
            state = storePinStateOf(
                count = uiState.storePins.find { it.storeId == selectedDetail.storeId }
                    ?.pinState?.count ?: 0,
                name = selectedDetail.storeName,
                isSelected = true,
            ),
            onTap = { onAction(HomeUiAction.SelectedStoreDismissed) },
            modifier = Modifier
                .onGloballyPositioned { pinSize = it.size }
                .offset {
                    if (pinSize == IntSize.Zero) {
                        IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
                    } else {
                        IntOffset(
                            x = (offset.x - pinSize.width / 2f).roundToInt(),
                            y = (offset.y - pinSize.height).roundToInt(),
                        )
                    }
                },
        )
    }
}

@Composable
private fun HomeListContent(
    uiState: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
            .padding(
                top = 12.dp,
            ),
    ) {
        LazyRow(
            modifier = Modifier
                .background(White),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            item {
                HomeCategoryChip(
                    content = stringResource(homeR.string.product_category_all),
                    isSelected = uiState.selectedCategory == null,
                    onClick = { onAction(HomeUiAction.CategorySelected(null)) },
                )
            }

            items(ProductCategory.entries.toList()) { category ->
                HomeCategoryChip(
                    content = stringResource(category.toLabelTextRes()),
                    isSelected = uiState.selectedCategory == category,
                    onClick = { onAction(HomeUiAction.CategorySelected(category)) },
                )
            }
        }

        val filteredGroups = remember(uiState.storeGroups, uiState.selectedCategory) {
            uiState.storeGroups.mapNotNull { group ->
                val filtered = if (uiState.selectedCategory == null) {
                    group.products
                } else {
                    group.products.filter { it.category == uiState.selectedCategory }
                }
                if (filtered.isEmpty()) null else group.copy(products = filtered)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SortDropdown(
                selectedOption = uiState.sortOption,
                onOptionSelected = { onAction(HomeUiAction.SortOptionSelected(it)) },
            )

            Text(
                text = stringResource(homeR.string.home_total_count, filteredGroups.sumOf { it.products.size }),
                style = MangroTheme.typography.label.labelS ?: MangroTheme.typography.label.labelM,
                color = MangroTheme.colors.textTitle,
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 50.dp),
        ) {
            filteredGroups.forEach { group ->
                item(key = "${group.storeId}_header") {
                    StoreGroupHeader(
                        storeName = group.storeName,
                        walkingMinutes = group.walkingMinutes,
                        productCount = group.products.size,
                        closingInMinutes = group.closingInMinutes,
                    )
                }
                items(group.products, key = { it.id }) { product ->
                    ProductListCard(
                        product = product,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(White)
                            .clickable { onAction(HomeUiAction.ListProductClicked(product.id)) }
                            .padding(
                                vertical = 16.dp,
                                horizontal = 20.dp,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopOverlay(
    uiState: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (uiState.viewMode == HomeViewMode.MAP && !uiState.isLocationPermissionGranted) {
            ActionBanner(
                iconRes = R.drawable.ic_error,
                stringRes = R.string.banner_location_permission,
                action = {
                    Text(
                        text = stringResource(R.string.banner_action_turn_on),
                        color = MangroTheme.colors.primaryNormal,
                        style = MangroTheme.typography.label.labelM,
                        modifier = Modifier.clickable {
                            onAction(HomeUiAction.PermissionBannerActionClicked)
                        },
                    )
                },
            )
        }

        if (uiState.viewMode == HomeViewMode.MAP) {
            uiState.activeWish?.let { wish ->
                CountdownCard(
                    storeName = wish.storeName,
                    productSummary = wish.productSummary,
                    requestTimeMillis = wish.requestTimeMillis,
                    endTimeMillis = wish.endTimeMillis,
                    onClick = { /* TODO */ },
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SelectedStoreCard(
    selectedStore: SelectedStoreDetail?,
    onAction: (HomeUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = selectedStore != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300),
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250),
        ) + fadeOut(animationSpec = tween(250)),
        modifier = modifier,
    ) {
        selectedStore?.let { store ->
            MapStoreCard(
                storeName = store.storeName,
                closingTime = store.closingTime,
                products = store.products,
                onProductClick = { onAction(HomeUiAction.ProductClicked(it)) },
                modifier = Modifier.padding(16.dp),
                travelInfo = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_directions_walk),
                            contentDescription = null,
                            tint = Gray900,
                        )
                        Text(
                            text = stringResource(homeR.string.home_walking_minutes, store.walkingMinutes),
                            style = MangroTheme.typography.caption.captionS,
                            color = MangroTheme.colors.textBody,
                        )
                    }
                },
            )
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
            HomeUiState(locationName = "망원동"),
            HomeUiState(locationName = "망원동", isLocationPermissionGranted = false),
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
