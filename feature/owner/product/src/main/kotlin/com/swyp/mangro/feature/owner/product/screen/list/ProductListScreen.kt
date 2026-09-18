package com.swyp.mangro.feature.owner.product.screen.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.appbar.OwnerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestCard
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestStatus
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProductCard
import com.swyp.mangro.core.designsystem.component.chip.MangroChip
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.data.owner.product.model.ProductSummary
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.ManagementLoadStatus
import com.swyp.mangro.feature.owner.product.model.OwnerPickupModel
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
data object OwnerProductListDestination

@Composable
internal fun ProductListRoute(
    products: List<OwnerProductModel>,
    onSelect: (String) -> Unit,
    onMenuClick: (OwnerMenu) -> Unit,
    onPickupClick: (String) -> Unit,
    onCancelReservations: (List<String>) -> Unit,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val pickups = viewModel.pickups.collectAsLazyPagingItems()
    val catalog = viewModel.products.collectAsLazyPagingItems()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshProducts()
    }

    LaunchedEffect(
        viewModel,
        lifecycleOwner,
        onSelect,
        onMenuClick,
        onPickupClick,
        onCancelReservations,
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    is ProductListEvent.OpenProduct -> onSelect(event.id)
                    is ProductListEvent.OpenMenu -> onMenuClick(event.menu)
                    is ProductListEvent.OpenPickup -> onPickupClick(event.id)
                    is ProductListEvent.CancelReservations -> onCancelReservations(event.productIds)
                }
            }
        }
    }

    ProductListScreen(
        uiState = state,
        onAction = viewModel::handleAction,
        pickups = pickups,
        products = catalog,
    )
}

@Composable
fun ProductListScreen(
    uiState: ProductListState,
    onAction: (ProductListAction) -> Unit,
    pickups: LazyPagingItems<OwnerPickupModel>,
    products: LazyPagingItems<ProductSummary>,
    modifier: Modifier = Modifier,
) {
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1000)
        }
    }
    val refresh = pickups.loadState.refresh
    val productRefresh = products.loadState.refresh
    val productListState = rememberLazyListState()
    val pickupListState = rememberLazyListState()
    LaunchedEffect(uiState.filter) { pickupListState.scrollToItem(0) }
    Scaffold(
        modifier = modifier,
        containerColor = MangroTheme.colors.surfaceNormal,
        topBar = {
            Column {
                MangroDefaultStartAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.owner_product_list_title),
                            style = MangroTheme.typography.heading.headingXXS,
                            color = MangroTheme.colors.textTitle,
                        )
                    },
                )
                StoreTabs(state = uiState, onAction = onAction)
            }
        },
        bottomBar = {
            OwnerBottomAppBar(
                currentMenu = OwnerMenu.STORE,
                onMenuClick = { onAction(ProductListAction.MenuSelected(it)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MangroTheme.colors.surfaceAlter),
        ) {
            if (uiState.tab == ProductListTab.PICKUPS) {
                LazyRow(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .selectableGroup(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(items = ProductListFilter.entries) { filter ->
                        MangroChip(
                            isOwner = true,
                            modifier = Modifier.heightIn(min = 34.dp),
                            content = stringResource(filter.labelRes()),
                            isSelected = uiState.filter == filter,
                            onClick = { onAction(ProductListAction.FilterSelected(filter)) },
                        )
                    }
                }
            }

            if (uiState.tab == ProductListTab.PICKUPS && refresh is LoadState.NotLoading && !uiState.hasPickupError) {
                Text(
                    text = stringResource(R.string.owner_product_filtered_count, uiState.filteredTotal),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    style = MangroTheme.typography.body.bodyM,
                    color = MangroTheme.colors.textSubtitle,
                )
            }

            if (uiState.tab == ProductListTab.PICKUPS) {
                ManagementLoadStatus(refresh is LoadState.Loading && pickups.itemCount == 0, refresh is LoadState.Error) { pickups.retry() }
                ManagementLoadStatus(false, uiState.hasPickupError) { onAction(ProductListAction.Refresh) }
            }

            if (uiState.tab == ProductListTab.PRODUCTS) {
                ManagementLoadStatus(productRefresh is LoadState.Loading, productRefresh is LoadState.Error, products::retry)
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val empty = if (uiState.tab == ProductListTab.PRODUCTS) {
                    products.itemCount == 0
                } else {
                    pickups.itemCount == 0
                }

                if (uiState.tab == ProductListTab.PRODUCTS && empty && productRefresh !is LoadState.NotLoading) {
                    // 로딩과 실패를 빈 목록으로 표시하지 않는다.
                } else if (uiState.tab == ProductListTab.PICKUPS && empty && (refresh is LoadState.Loading || refresh is LoadState.Error || uiState.hasPickupError)) {
                    // 오류를 빈 목록으로 표시하지 않는다.
                } else if (empty) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(bottom = 144.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_error),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MangroTheme.colors.textCanceled,
                        )
                        Text(
                            text = stringResource(R.string.owner_product_empty_filter_title),
                            color = MangroTheme.colors.textSubtitle,
                            style = MangroTheme.typography.body.bodyL,
                        )
                    }
                } else {
                    LazyColumn(
                        state = if (uiState.tab == ProductListTab.PRODUCTS) productListState else pickupListState,
                        modifier = Modifier.fillMaxSize().testTag("owner-holds-list"),
                        contentPadding = PaddingValues(bottom = if (uiState.cancellationCount == 0) 0.dp else 112.dp),
                    ) {
                        if (uiState.tab == ProductListTab.PRODUCTS) {
                            item {
                                ManagementLoadStatus(
                                    products.loadState.prepend is LoadState.Loading,
                                    products.loadState.prepend is LoadState.Error,
                                    products::retry,
                                )
                            }
                            items(
                                count = products.itemCount,
                            ) { index ->
                                val product = products[index] ?: return@items
                                Column(
                                    modifier = Modifier
                                        .background(MangroTheme.colors.surfaceNormal)
                                        .padding(horizontal = 20.dp),
                                ) {
                                    OwnerProductCard(
                                        product = OwnerProduct(
                                            id = product.id.toString(),
                                            imageUrl = product.photoUrl,
                                            name = product.name,
                                            price = product.salePrice,
                                            remainingCount = product.availableQuantity,
                                            expectedVisitCount = product.activeHoldQuantity,
                                            shortfallQty = product.shortfallQuantity,
                                        ),
                                        modifier = Modifier
                                            .clickable {
                                                onAction(ProductListAction.ProductClicked(product.id.toString()))
                                            }
                                            .padding(vertical = 24.dp),
                                    )
                                    HorizontalDivider(color = MangroTheme.colors.borderDefault)
                                }
                            }
                            item {
                                ManagementLoadStatus(
                                    products.loadState.append is LoadState.Loading,
                                    products.loadState.append is LoadState.Error,
                                    products::retry,
                                )
                            }
                        } else {
                            if (pickups.loadState.prepend is LoadState.Loading || pickups.loadState.prepend is LoadState.Error) {
                                item {
                                    ManagementLoadStatus(
                                        pickups.loadState.prepend is LoadState.Loading,
                                        pickups.loadState.prepend is LoadState.Error,
                                        pickups::retry,
                                    )
                                }
                            }
                            items(
                                count = pickups.itemCount,
                                key = pickups.itemKey { it.request.id },
                            ) { index ->
                                val loaded = pickups[index] ?: return@items
                                val expired = loaded.request.status == OwnerPickupRequestStatus.IN_PROGRESS &&
                                    loaded.request.endTimeMillis?.let { it <= now } == true
                                val pickup = if (expired) {
                                    loaded.copy(request = loaded.request.copy(status = OwnerPickupRequestStatus.EXPIRED, requestTimeMillis = null, endTimeMillis = null), canComplete = false)
                                } else {
                                    loaded
                                }
                                OwnerPickupRequestCard(
                                    item = pickup.request,
                                    completeEnabled = pickup.canComplete && !uiState.mutationInProgress && !uiState.hasPickupError && refresh is LoadState.NotLoading,
                                    onConsumerClick = { onAction(ProductListAction.PickupClicked(pickup.request.id)) },
                                    onButtonClick = { onAction(ProductListAction.PickupCompleteClicked(pickup)) },
                                    modifier = Modifier.padding(bottom = 16.dp),
                                )
                            }
                            item {
                                ManagementLoadStatus(
                                    pickups.loadState.append is LoadState.Loading,
                                    pickups.loadState.append is LoadState.Error,
                                    pickups::retry,
                                )
                            }
                        }
                    }
                }
                if (uiState.cancellationCount > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MangroTheme.colors.grayScale900,
                        shadowElevation = 8.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 20.dp,
                                    end = 12.dp,
                                    top = 16.dp,
                                    bottom = 16.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(DesignR.drawable.ic_error),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MangroTheme.colors.primaryNormal,
                            )
                            Text(
                                text = stringResource(
                                    R.string.owner_product_cancellation_needed_count,
                                    uiState.cancellationCount,
                                ),
                                modifier = Modifier.weight(1f),
                                color = MangroTheme.colors.textOnBrandWhite,
                                style = MangroTheme.typography.label.labelS ?: MangroTheme.typography.label.labelM,
                            )
                            Text(
                                text = stringResource(R.string.owner_product_cancel_action),
                                modifier = Modifier
                                    .clickable(
                                        role = Role.Button,
                                        onClick = { onAction(ProductListAction.ReservationsCancelClicked) },
                                    )
                                    .padding(8.dp),
                                color = MangroTheme.colors.textOnBrandWhite,
                                style = MangroTheme.typography.label.labelM,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreTabs(
    state: ProductListState,
    onAction: (ProductListAction) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MangroTheme.colors.surfaceNormal)
            .selectableGroup(),
    ) {
        ProductListTab.entries.forEach { tab ->
            val selected = state.tab == tab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onAction(ProductListAction.TabSelected(tab)) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (tab == ProductListTab.PICKUPS && state.pickups.any { it.request.isNew }) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = MangroTheme.colors.primaryNormal,
                                        shape = RoundedCornerShape(50),
                                    ),
                            )
                        }
                        Text(
                            text = when {
                                tab == ProductListTab.PRODUCTS -> stringResource(R.string.owner_product_registered_title)
                                state.totalHolds == null -> stringResource(R.string.owner_product_pickup_title)
                                else -> stringResource(R.string.owner_product_pickup_count, state.totalHolds)
                            },
                            color = if (selected) MangroTheme.colors.primaryNormal else MangroTheme.colors.textCanceled,
                            style = MangroTheme.typography.label.labelM,
                        )
                    }
                }
                HorizontalDivider(
                    thickness = 2.dp,
                    color = if (selected) MangroTheme.colors.primaryNormal else MangroTheme.colors.surfaceAlter,
                )
            }
        }
    }
}

private fun ProductListFilter.labelRes(): Int = when (this) {
    ProductListFilter.ALL -> R.string.owner_product_filter_all
    ProductListFilter.COMPLETED -> R.string.owner_product_filter_completed
    ProductListFilter.UNAVAILABLE -> R.string.owner_product_filter_unavailable
    ProductListFilter.CANCELLED -> R.string.owner_product_filter_cancelled
    ProductListFilter.EXPIRED -> R.string.owner_product_filter_expired
}
