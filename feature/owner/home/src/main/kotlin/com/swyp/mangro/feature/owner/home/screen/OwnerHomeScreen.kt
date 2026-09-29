package com.swyp.mangro.feature.owner.home.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.component.banner.ActionBanner
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProductCard
import com.swyp.mangro.core.designsystem.component.label.MangroLabel
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.feature.owner.home.R
import com.swyp.mangro.feature.owner.home.component.card.DashboardCard
import com.swyp.mangro.feature.owner.home.component.card.VisitorCard
import com.swyp.mangro.feature.owner.home.component.heading.SectionHeading
import com.swyp.mangro.feature.owner.home.component.section.AttentionSection
import com.swyp.mangro.feature.owner.home.component.section.PendingStoreSection
import com.swyp.mangro.feature.owner.home.component.section.RegisterNewProductSection
import com.swyp.mangro.feature.owner.home.component.section.RejectStoreSection
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeAction
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeEvent
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeUiState
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeVisitor
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.coroutines.delay

@Composable
fun OwnerHomeScreenRoute(
    navigateToProducts: () -> Unit,
    navigateToPickups: (completedOnly: Boolean) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToProduct: (String) -> Unit,
    navigateToPickup: (String) -> Unit,
    navigateToRegisterProduct: () -> Unit,
    viewModel: OwnerHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is OwnerHomeEvent.ShowMessage -> snackbarHostState.showSnackbar(context.getString(event.message))
                OwnerHomeEvent.NavigateToSettings -> navigateToSettings()
                OwnerHomeEvent.NavigateToProducts -> navigateToProducts()
                is OwnerHomeEvent.NavigateToPickups -> navigateToPickups(event.completedOnly)
                is OwnerHomeEvent.NavigateToPickup -> navigateToPickup(event.pickupId)
                is OwnerHomeEvent.NavigateToProduct -> navigateToProduct(event.productId)
                OwnerHomeEvent.NavigateToRegisterProduct -> navigateToRegisterProduct()
            }
        }
    }

    OwnerHomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::handleAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    uiState: OwnerHomeUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (OwnerHomeAction) -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val nowMillis by produceState(System.currentTimeMillis(), lifecycleOwner, uiState.visitors.isNotEmpty()) {
        if (uiState.visitors.isNotEmpty()) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    value = System.currentTimeMillis()
                    delay(1_000.milliseconds)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                MangroDefaultStartAlignedTopAppBar(
                    modifier = Modifier.background(MangroTheme.colors.surfaceNormal),
                    title = {
                        Text(
                            text = stringResource(R.string.owner_home_title),
                            style = MangroTheme.typography.label.labelL,
                            color = MangroTheme.colors.textTitle,
                        )
                    },
                    actions = { /* Nothing Implemented */ },
                )

                AnimatedVisibility(visible = uiState.hasNewPickup) {
                    ActionBanner(
                        iconRes = DesignR.drawable.ic_alert,
                        stringRes = DesignR.string.banner_new_like,
                        iconSize = 24.dp,
                        modifier = Modifier.heightIn(min = 56.dp),
                    ) {
                        Text(
                            text = stringResource(DesignR.string.banner_action_confirm),
                            style = MangroTheme.typography.caption.captionS,
                            color = MangroTheme.colors.primaryNormal,
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { onAction(OwnerHomeAction.ViewNewPickups) },
                                )
                                .padding(8.dp),
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    containerColor = MangroTheme.colors.grayScale900,
                    contentColor = MangroTheme.colors.textOnBrandWhite,
                ) {
                    Text(
                        text = data.visuals.message,
                        style = MangroTheme.typography.caption.captionS,
                    )
                }
            }
        },
        containerColor = MangroTheme.colors.surfaceAlter,
        floatingActionButton = {
            if (uiState.hasRegisteredProduct && uiState.canRegisterProduct && !uiState.isLoading && uiState.errorMessage == null) {
                ExtendedFloatingActionButton(
                    onClick = { onAction(OwnerHomeAction.RegisterProduct) },
                    shape = CircleShape,
                    containerColor = MangroTheme.colors.primaryNormal,
                    contentColor = MangroTheme.colors.textOnBrandWhite,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_plus_24px),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MangroTheme.colors.textOnBrandWhite,
                    )
                    Text(
                        text = stringResource(R.string.owner_home_register),
                        style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullToRefresh(
                    state = pullToRefreshState,
                    isRefreshing = uiState.hasRegisteredProduct && uiState.isLoading,
                    enabled = uiState.hasRegisteredProduct,
                    onRefresh = { onAction(OwnerHomeAction.Refresh) },
                ),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = if (uiState.hasRegisteredProduct) 88.dp else 44.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                if (uiState.isLoading) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(color = MangroTheme.colors.primaryNormal)
                        }
                    }
                    return@LazyColumn
                }

                if (uiState.errorMessage != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.empty_visitors),
                                contentDescription = null,
                            )

                            Text(
                                text = stringResource(uiState.errorMessage),
                                textAlign = TextAlign.Center,
                                style = MangroTheme.typography.title.titleM.copy(lineBreak = LineBreak.Heading),
                            )

                            MangroButton(
                                text = stringResource(R.string.owner_home_retry),
                                onClick = { onAction(OwnerHomeAction.Refresh) },
                                style = MangroButtonStyle.OUTLINED,
                            )
                        }
                    }
                    return@LazyColumn
                }

                if (!uiState.hasRegisteredProduct) {
                    item {
                        when (uiState.approvalStatus) {
                            StoreApprovalStatus.PENDING -> {
                                PendingStoreSection()
                            }

                            StoreApprovalStatus.REJECTED -> {
                                RejectStoreSection()
                            }

                            StoreApprovalStatus.APPROVED -> {
                                RegisterNewProductSection(
                                    enabled = uiState.canRegisterProduct,
                                    onClick = { onAction(OwnerHomeAction.RegisterProduct) },
                                )
                            }

                            else -> { /* Do Nothing */ }
                        }
                    }
                } else {
                    item {
                        AttentionSection(
                            hasAttention = uiState.hasAttention || !uiState.attentionAvailable,
                            isShowAttention = uiState.showAttention,
                            cancellationRequiredCount = uiState.cancellationRequiredCount,
                            needsPickupConfirmation = uiState.needsPickupConfirmation,
                            onAction = onAction,
                        )
                    }

                    dashboardSection(
                        name = uiState.storeName,
                        category = uiState.storeCategory,
                        expectedVisitCount = uiState.expectedVisitCount,
                        completedPickupCount = uiState.completedPickupCount,
                        sellingProductsCount = uiState.sellingCount,
                        onViewPickups = { onAction(OwnerHomeAction.ViewPickups) },
                        onViewCompletedPickups = { onAction(OwnerHomeAction.ViewCompletedPickups) },
                        onViewSellingProducts = { onAction(OwnerHomeAction.ViewProducts) },
                    )

                    pickupSection(
                        nowMillis = nowMillis,
                        completingPickupIds = uiState.completingPickupIds,
                        visitors = uiState.visitors,
                        onViewAll = { onAction(OwnerHomeAction.ViewPickups) },
                        onViewPickup = { onAction(OwnerHomeAction.ViewPickup(it)) },
                        onMarkAsPickedUp = { onAction(OwnerHomeAction.MarkAsPickedUp(it)) },
                    )

                    productsSection(
                        canRegisterProduct = uiState.canRegisterProduct,
                        products = uiState.products,
                        onViewAll = { onAction(OwnerHomeAction.ViewProducts) },
                        onViewProduct = { onAction(OwnerHomeAction.ViewProduct(it)) },
                        onRegisterProduct = { onAction(OwnerHomeAction.RegisterProduct) },
                    )
                }
            }

            if (uiState.hasRegisteredProduct) {
                Indicator(
                    state = pullToRefreshState,
                    isRefreshing = uiState.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = MangroTheme.colors.surfaceNormal,
                    color = MangroTheme.colors.primaryNormal,
                )
            }
        }
    }
}

private fun LazyListScope.dashboardSection(
    name: String,
    category: String,
    expectedVisitCount: Int,
    completedPickupCount: Long,
    sellingProductsCount: Int,
    onViewPickups: () -> Unit,
    onViewCompletedPickups: () -> Unit,
    onViewSellingProducts: () -> Unit,
) {
    item {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_store_front),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MangroTheme.colors.textTitle,
                )
                Text(
                    text = name,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MangroTheme.typography.body.bodyL,
                    color = MangroTheme.colors.textTitle,
                )
                MangroLabel(
                    content = category,
                    contentColor = MangroTheme.colors.vegetablesNormal,
                    containerColor = MangroTheme.colors.vegetablesBg,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DashboardCard(
                    title = stringResource(R.string.owner_home_expected),
                    value = stringResource(R.string.owner_home_cases, expectedVisitCount),
                    drawResId = DesignR.drawable.ic_owner_heart,
                    startColor = Color(0xFFFFB480),
                    endColor = MangroTheme.colors.primaryLight,
                    modifier = Modifier.weight(1f),
                    onClick = onViewPickups,
                )

                DashboardCard(
                    title = stringResource(R.string.owner_home_completed),
                    value = stringResource(R.string.owner_home_cases, completedPickupCount),
                    drawResId = DesignR.drawable.ic_pickup,
                    startColor = Color(0xFFFFDD7D),
                    endColor = MangroTheme.colors.primaryLight,
                    modifier = Modifier.weight(1f),
                    onClick = onViewCompletedPickups,
                )

                DashboardCard(
                    title = stringResource(R.string.owner_home_selling),
                    value = stringResource(R.string.owner_home_units, sellingProductsCount),
                    drawResId = DesignR.drawable.ic_register,
                    startColor = MangroTheme.colors.vegetablesNormal,
                    endColor = MangroTheme.colors.vegetablesBg,
                    modifier = Modifier.weight(1f),
                    onClick = onViewSellingProducts,
                )
            }
        }
    }
}

private fun LazyListScope.pickupSection(
    nowMillis: Long,
    completingPickupIds: PersistentSet<String>,
    visitors: PersistentList<OwnerHomeVisitor>,
    onViewAll: () -> Unit,
    onViewPickup: (id: String) -> Unit,
    onMarkAsPickedUp: (id: String) -> Unit,
) {
    item {
        SectionHeading(
            title = stringResource(if (visitors.isEmpty()) R.string.owner_home_no_visitors_title else R.string.owner_home_visitors),
            onViewAll = if (visitors.isEmpty()) {
                null
            } else {
                onViewAll
            },
            modifier = Modifier.padding(top = 32.dp),
        )
    }

    if (visitors.isEmpty()) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.empty_visitors),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    text = stringResource(R.string.owner_home_no_visitors),
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.textSubtitle,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 12.dp),
            ) {
                items(
                    items = visitors,
                    key = { it.id },
                ) { visitor ->
                    VisitorCard(
                        nowMillis = nowMillis,
                        visitor = visitor,
                        isCompleting = visitor.id in completingPickupIds,
                        onViewPickup = { onViewPickup(visitor.id) },
                        onMarkAsPickedUp = { onMarkAsPickedUp(visitor.id) },
                    )
                }
            }
        }
    }
}

private fun LazyListScope.productsSection(
    canRegisterProduct: Boolean,
    products: PersistentList<OwnerProduct>,
    onViewAll: () -> Unit,
    onViewProduct: (id: String) -> Unit,
    onRegisterProduct: () -> Unit,
) {
    item {
        SectionHeading(
            title = stringResource(R.string.owner_home_products),
            onViewAll = if (products.isEmpty()) {
                null
            } else {
                onViewAll
            },
            modifier = Modifier.padding(top = 48.dp),
        )
    }

    if (products.isEmpty()) {
        item {
            Column(
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.empty_products),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    text = stringResource(R.string.owner_home_no_products),
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MangroTheme.typography.caption.captionS,
                    color = MangroTheme.colors.textSubtitle,
                    textAlign = TextAlign.Center,
                )
            }

            MangroButton(
                text = stringResource(R.string.owner_home_register_button),
                onClick = onRegisterProduct,
                style = MangroButtonStyle.OUTLINED,
                enabled = canRegisterProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textStyle = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
            )
        }
    } else {
        items(
            items = products,
            key = { "product-${it.id}" },
        ) { product ->
            OwnerProductCard(
                product = product,
                modifier = Modifier
                    .clickable(onClick = { onViewProduct(product.id) })
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            )
            HorizontalDivider(color = MangroTheme.colors.borderDefault)
        }
    }
}
