package com.swyp.mangro.feature.owner.home.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.swyp.mangro.core.designsystem.component.appbar.OwnerBottomAppBar
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.core.designsystem.component.banner.ActionBanner
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProductCard
import com.swyp.mangro.core.designsystem.component.label.MangroLabel
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.home.R
import com.swyp.mangro.feature.owner.home.screen.component.AttentionSection
import com.swyp.mangro.feature.owner.home.screen.component.RegisterNewProductSection
import com.swyp.mangro.feature.owner.home.screen.component.card.DashboardCard
import com.swyp.mangro.feature.owner.home.screen.component.card.VisitorCard
import com.swyp.mangro.feature.owner.home.screen.component.heading.SectionHeading
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeAction
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeEvent
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeUiState
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
fun OwnerHomeScreenRoute(
    viewModel: OwnerHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                OwnerHomeEvent.NavigateToDetail -> { }
            }
        }
    }

    OwnerHomeScreen(
        uiState = uiState,
        onAction = viewModel::handleAction,
    )
}

@Composable
fun OwnerHomeScreen(
    uiState: OwnerHomeUiState,
    onAction: (OwnerHomeAction) -> Unit,
) {
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
                    actions = {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_owner_notification),
                            contentDescription = stringResource(R.string.owner_home_notifications),
                            tint = MangroTheme.colors.textTitle,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { onAction(OwnerHomeAction.ViewNotifications) },
                                ),
                        )
                    },
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
        bottomBar = {
            OwnerBottomAppBar(
                currentMenu = OwnerMenu.HOME,
                onMenuClick = {
                    if (it != OwnerMenu.HOME) return@OwnerBottomAppBar
                },
            )
        },
        containerColor = MangroTheme.colors.surfaceAlter,
        floatingActionButton = {
            if (uiState.hasRegisteredProduct) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = if (uiState.hasRegisteredProduct) 88.dp else 44.dp),
        ) {
            if (!uiState.hasRegisteredProduct) {
                item {
                    RegisterNewProductSection { onAction(OwnerHomeAction.RegisterProduct) }
                }
            } else {
                item {
                    AttentionSection(
                        hasAttention = uiState.hasAttention,
                        isShowAttention = uiState.showAttention,
                        cancellationRequiredCount = uiState.cancellationRequiredCount,
                        needsPickupConfirmation = uiState.needsPickupConfirmation,
                        onAction = onAction,
                    )
                }

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
                                text = uiState.storeName,
                                modifier = Modifier.weight(1f, fill = false),
                                style = MangroTheme.typography.body.bodyL,
                                color = MangroTheme.colors.textTitle,
                            )
                            MangroLabel(
                                content = uiState.storeCategory,
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
                                value = stringResource(R.string.owner_home_cases, uiState.expectedVisitCount),
                                drawResId = DesignR.drawable.ic_owner_heart,
                                startColor = Color(0xFFFFB480),
                                endColor = MangroTheme.colors.primaryLight,
                                modifier = Modifier.weight(1f),
                                onClick = { onAction(OwnerHomeAction.ViewPickups) },
                            )

                            DashboardCard(
                                title = stringResource(R.string.owner_home_completed),
                                value = stringResource(R.string.owner_home_cases, uiState.completedPickupCount),
                                drawResId = DesignR.drawable.ic_pickup,
                                startColor = Color(0xFFFFDD7D),
                                endColor = MangroTheme.colors.primaryLight,
                                modifier = Modifier.weight(1f),
                                onClick = { onAction(OwnerHomeAction.ViewCompletedPickups) },
                            )

                            DashboardCard(
                                title = stringResource(R.string.owner_home_selling),
                                value = stringResource(R.string.owner_home_units, uiState.sellingCount),
                                drawResId = DesignR.drawable.ic_register,
                                startColor = MangroTheme.colors.vegetablesNormal,
                                endColor = MangroTheme.colors.vegetablesBg,
                                modifier = Modifier.weight(1f),
                                onClick = { onAction(OwnerHomeAction.ViewProducts) },
                            )
                        }
                    }
                }

                item {
                    SectionHeading(
                        title = stringResource(if (uiState.visitors.isEmpty()) R.string.owner_home_no_visitors_title else R.string.owner_home_visitors),
                        onViewAll = if (uiState.visitors.isEmpty()) {
                            null
                        } else {
                            { onAction(OwnerHomeAction.ViewPickups) }
                        },
                        modifier = Modifier.padding(top = 32.dp),
                    )
                }

                if (uiState.visitors.isEmpty()) {
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
                            items(uiState.visitors, key = { it.id }) { visitor ->
                                VisitorCard(
                                    nowMillis = nowMillis,
                                    visitor = visitor,
                                    onAction = onAction,
                                )
                            }
                        }
                    }
                }
                item {
                    SectionHeading(
                        title = stringResource(R.string.owner_home_products),
                        onViewAll = if (uiState.products.isEmpty()) {
                            null
                        } else {
                            { onAction(OwnerHomeAction.ViewProducts) }
                        },
                        modifier = Modifier.padding(top = 48.dp),
                    )
                }

                if (uiState.products.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(top = 32.dp, bottom = 24.dp).fillMaxWidth(),
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
                            onClick = { onAction(OwnerHomeAction.RegisterProduct) },
                            style = MangroButtonStyle.OUTLINED,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            textStyle = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                        )
                    }
                } else {
                    items(
                        items = uiState.products,
                        key = { "product-${it.id}" },
                    ) { product ->
                        OwnerProductCard(
                            product = product,
                            modifier = Modifier
                                .clickable(onClick = { onAction(OwnerHomeAction.ViewProduct(product.id)) })
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                        )
                        HorizontalDivider(color = MangroTheme.colors.borderDefault)
                    }
                }
            }
        }
    }
}
