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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.appbar.OwnerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestCard
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProductCard
import com.swyp.mangro.core.designsystem.component.chip.MangroChip
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.model.OwnerPickupModel
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import kotlinx.serialization.Serializable

@Serializable
data object OwnerProductListDestination

@Composable
internal fun ProductListRoute(
    products: List<OwnerProductModel>,
    pickups: List<OwnerPickupModel>,
    onSelect: (String) -> Unit,
    onMenuClick: (OwnerMenu) -> Unit,
    onPickupClick: (String) -> Unit,
    onCompletePickup: (String) -> Unit,
    onCancelReservations: (List<String>) -> Unit,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(products, pickups) {
        viewModel.updateContent(
            products = products,
            pickups = pickups,
        )
    }
    LaunchedEffect(
        viewModel,
        lifecycleOwner,
        onSelect,
        onMenuClick,
        onPickupClick,
        onCompletePickup,
        onCancelReservations,
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    is ProductListEvent.OpenProduct -> onSelect(event.id)
                    is ProductListEvent.OpenMenu -> onMenuClick(event.menu)
                    is ProductListEvent.OpenPickup -> onPickupClick(event.id)
                    is ProductListEvent.CompletePickup -> onCompletePickup(event.id)
                    is ProductListEvent.CancelReservations -> onCancelReservations(event.productIds)
                }
            }
        }
    }

    ProductListScreen(
        uiState = state,
        onAction = viewModel::handleAction,
    )
}

@Composable
fun ProductListScreen(
    uiState: ProductListState,
    onAction: (ProductListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MangroTheme.colors.surfaceNormal,
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
            Text(
                text = stringResource(R.string.owner_product_list_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MangroTheme.colors.surfaceNormal)
                    .padding(20.dp),
                style = MangroTheme.typography.heading.headingXXS,
                color = MangroTheme.colors.textTitle,
            )
            StoreTabs(
                state = uiState,
                onAction = onAction,
            )
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
            Text(
                text = stringResource(
                    R.string.owner_product_filtered_count,
                    if (uiState.tab == ProductListTab.PRODUCTS) {
                        uiState.filteredProducts.size
                    } else {
                        uiState.filteredPickups.size
                    },
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                style = MangroTheme.typography.body.bodyM,
                color = MangroTheme.colors.textSubtitle,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val empty = if (uiState.tab == ProductListTab.PRODUCTS) {
                    uiState.filteredProducts.isEmpty()
                } else {
                    uiState.filteredPickups.isEmpty()
                }
                if (empty) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 144.dp),
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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = if (uiState.cancellationNeeded.isEmpty()) 0.dp else 112.dp),
                    ) {
                        if (uiState.tab == ProductListTab.PRODUCTS) {
                            items(
                                items = uiState.filteredProducts,
                                key = { it.id },
                            ) { product ->
                                Column(
                                    modifier = Modifier
                                        .background(MangroTheme.colors.surfaceNormal)
                                        .padding(horizontal = 20.dp),
                                ) {
                                    OwnerProductCard(
                                        product = OwnerProduct(
                                            id = product.id,
                                            imageUrl = product.photos.firstOrNull().orEmpty(),
                                            name = product.name,
                                            price = product.salePrice,
                                            remainingCount = product.remainingQuantity,
                                            expectedVisitCount = product.reservedQuantity,
                                            unableToPurchaseCount = uiState.cancellationNeeded.count {
                                                it.productId == product.id
                                            },
                                        ),
                                        modifier = Modifier
                                            .clickable {
                                                onAction(ProductListAction.ProductClicked(product.id))
                                            }
                                            .padding(vertical = 24.dp),
                                    )
                                    HorizontalDivider(color = MangroTheme.colors.borderDefault)
                                }
                            }
                        } else {
                            items(
                                items = uiState.filteredPickups,
                                key = { it.request.id },
                            ) { pickup ->
                                OwnerPickupRequestCard(
                                    item = pickup.request,
                                    onConsumerClick = { onAction(ProductListAction.PickupClicked(pickup.request.id)) },
                                    onButtonClick = { onAction(ProductListAction.PickupCompleteClicked(pickup.request.id)) },
                                    modifier = Modifier.padding(bottom = 16.dp),
                                )
                            }
                        }
                    }
                }
                if (uiState.cancellationNeeded.isNotEmpty()) {
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
                                    uiState.cancellationNeeded.size,
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
                            text = stringResource(
                                if (tab == ProductListTab.PRODUCTS) {
                                    R.string.owner_product_registered_count
                                } else {
                                    R.string.owner_product_pickup_count
                                },
                                if (tab == ProductListTab.PRODUCTS) {
                                    state.products.size
                                } else {
                                    state.pickups.size
                                },
                            ),
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
