package com.swyp.mangro.feature.owner.product.screen.pickup.cancellation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.R as DesignR
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.MangroCheckbox
import com.swyp.mangro.core.designsystem.component.appbar.MangroDefaultStartAlignedTopAppBar
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.PickupCancellationSheet
import com.swyp.mangro.feature.owner.product.model.pickupDate
import kotlinx.serialization.Serializable

@Serializable
data class OwnerPickupCancellationDestination(val productIds: List<String> = emptyList())

@Composable
internal fun PickupCancellationRoute(
    navigateBack: () -> Unit,
    viewModel: PickupCancellationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler { viewModel.handleAction(PickupCancellationAction.NavigationBackClicked) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                PickupCancellationEvent.NavigateBack -> navigateBack()
            }
        }
    }

    PickupCancellationScreen(
        uiState = state,
        onAction = viewModel::handleAction,
    )
}

@Composable
internal fun PickupCancellationScreen(
    uiState: PickupCancellationState,
    onAction: (PickupCancellationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MangroDefaultStartAlignedTopAppBar(
                modifier = Modifier.background(MangroTheme.colors.surfaceNormal),
                title = {
                    Text(
                        text = stringResource(R.string.pickup_cancel_title),
                        style = MangroTheme.typography.heading.headingXXS,
                        color = MangroTheme.colors.textTitle,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(PickupCancellationAction.NavigationBackClicked) }) {
                        Icon(
                            painter = painterResource(DesignR.drawable.ic_arrow_left),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp),
            ) {
                if (uiState.hasError) {
                    Text(
                        text = stringResource(R.string.pickup_error),
                        color = MangroTheme.colors.dangerNormal,
                    )
                }

                MangroButton(
                    text = stringResource(R.string.pickup_cancel_count_action, uiState.selectedIds.size),
                    onClick = { onAction(PickupCancellationAction.CancelClicked) },
                    style = MangroButtonStyle.ACTIVE,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.selectedIds.isNotEmpty(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                )
            }
        },
        containerColor = MangroTheme.colors.surfaceAlter,
    ) { padding ->
        if (uiState.shortages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                CancellationHeading(
                    count = 0,
                    modifier = Modifier.padding(20.dp),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.pickup_empty),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = stringResource(R.string.pickup_cancel_empty),
                        modifier = Modifier.padding(top = 12.dp, bottom = 60.dp),
                        style = MangroTheme.typography.body.bodyM,
                        color = MangroTheme.colors.textSubtitle,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(
                        Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .fillMaxWidth()
                            .background(
                                color = MangroTheme.colors.dangerBg,
                                shape = RoundedCornerShape(16.dp),
                            ).border(
                                width = 1.dp,
                                color = MangroTheme.colors.dangerNormal.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                            ).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.pickup_shortage),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            text = stringResource(R.string.pickup_shortage_summary, uiState.shortages.size, uiState.targets.size),
                            style = MangroTheme.typography.body.bodyM,
                            textAlign = TextAlign.Center,
                            color = MangroTheme.colors.dangerNormal,
                        )
                    }
                    CancellationHeading(
                        count = uiState.selectedIds.size,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    )
                }

                items(
                    items = uiState.shortages,
                    key = { it.productId },
                ) { shortage ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MangroTheme.colors.surfaceNormal)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.pickup_product),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = shortage.productName,
                                style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                                color = MangroTheme.colors.textTitle,
                            )
                        }

                        shortage.targets.forEach { pickup ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MangroTheme.colors.surfaceAlter,
                                        shape = RoundedCornerShape(12.dp),
                                    ).toggleable(
                                        value = pickup.id in uiState.selectedIds,
                                        role = Role.Checkbox,
                                        onValueChange = { onAction(PickupCancellationAction.SelectionChanged(pickup.id, it)) },
                                    ).padding(
                                        horizontal = 12.dp,
                                        vertical = 16.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                MangroCheckbox(
                                    isChecked = pickup.id in uiState.selectedIds,
                                    onCheckedChange = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.pickup_position, shortage.requestPositions.getValue(pickup.id), pickupDate(pickup.requestedAt)),
                                        style = MangroTheme.typography.caption.captionS,
                                        color = MangroTheme.colors.textSubtitle,
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        if (!pickup.notificationsEnabled) {
                                            Image(
                                                painter = painterResource(R.drawable.pickup_notifications_off),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                        Text(
                                            text = stringResource(R.string.pickup_cancel_customer_quantity, pickup.customerName, pickup.quantity),
                                            style = MangroTheme.typography.title.titleS ?: MangroTheme.typography.title.titleM,
                                            color = MangroTheme.colors.textTitle,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showConfirmation && uiState.selectedIds.isNotEmpty()) {
        PickupCancellationSheet(uiState = uiState, onAction = onAction)
    }
}

@Composable
private fun CancellationHeading(count: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.pickup_cancel_pending_prefix))
                withStyle(SpanStyle(color = MangroTheme.colors.primaryNormal)) {
                    append(stringResource(R.string.pickup_cases, count))
                }
            },
            style = MangroTheme.typography.heading.headingXXS,
            color = MangroTheme.colors.textTitle,
        )
        Text(
            text = stringResource(R.string.pickup_allocation_rule),
            style = MangroTheme.typography.caption.captionS,
            color = MangroTheme.colors.textSubtitle,
        )
    }
}
