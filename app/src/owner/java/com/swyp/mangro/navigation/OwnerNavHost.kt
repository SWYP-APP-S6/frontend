package com.swyp.mangro.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.swyp.mangro.OwnerMainViewModel
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.dialog.MangroDialogContainer
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.feature.owner.home.navigation.OwnerHomeDestination
import com.swyp.mangro.feature.owner.home.navigation.ownerHomeNavGraph
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.navigation.OwnerProductEditorDestination
import com.swyp.mangro.feature.owner.product.navigation.navigateToOwnerPickups
import com.swyp.mangro.feature.owner.product.navigation.ownerPickupNavGraph
import com.swyp.mangro.feature.owner.product.navigation.ownerProductNavGraph
import com.swyp.mangro.feature.owner.product.screen.detail.OwnerProductDetailDestination
import com.swyp.mangro.feature.owner.product.screen.list.OwnerProductListDestination
import com.swyp.mangro.feature.owner.product.screen.list.ProductListFilter
import com.swyp.mangro.feature.owner.product.screen.pickup.cancellation.OwnerPickupCancellationDestination
import com.swyp.mangro.feature.owner.product.screen.pickup.detail.OwnerPickupDetailDestination
import com.swyp.mangro.feature.owner.setting.navigation.OwnerPolicyDestination
import com.swyp.mangro.feature.owner.setting.navigation.OwnerSettingDestination
import com.swyp.mangro.feature.owner.setting.navigation.ownerSettingNavGraph
import com.swyp.mangro.notification.model.OwnerNotificationOpen
import com.swyp.mangro.notification.model.OwnerNotificationType

@Composable
internal fun OwnerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    notificationOpen: OwnerNotificationOpen? = null,
    ownerMainViewModel: OwnerMainViewModel = hiltViewModel(),
    onNotificationOpened: () -> Unit = {},
    onLogout: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val stockReconfirmationState by ownerMainViewModel.stockReconfirmationState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(ownerMainViewModel) {
        ownerMainViewModel.onMainResumed()
        onPauseOrDispose {
            ownerMainViewModel.onMainPaused()
        }
    }

    LaunchedEffect(notificationOpen, ownerMainViewModel) {
        if (notificationOpen != null) {
            when (notificationOpen.type) {
                OwnerNotificationType.STOCK_RECONFIRM_REQUEST -> notificationOpen.productId?.let { productId ->
                    ownerMainViewModel.openStockReconfirmation(notificationOpen.key, productId)
                }

                else -> navController.navigateToOwnerPickups(
                    if (notificationOpen.type == OwnerNotificationType.HOLD_EXPIRED) ProductListFilter.EXPIRED else ProductListFilter.ALL,
                ) {
                    popUpTo<OwnerHomeDestination> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            onNotificationOpened()
        }
    }

    LaunchedEffect(lifecycleOwner, ownerMainViewModel) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            ownerMainViewModel.editProduct.collect {
                navController.navigate(OwnerProductDetailDestination(it.toString()))
            }
        }
    }

    MangroDialogContainer(
        show = stockReconfirmationState.product != null,
        onDismissRequest = ownerMainViewModel::dismissStockReconfirmation,
        contentSpacing = 0.dp,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.owner_stock_reconfirm_title))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stockReconfirmationState.product?.quantity?.toString().orEmpty(),
                        color = MangroTheme.colors.primaryNormal,
                        style = MangroTheme.typography.title.titleL.copy(fontSize = 24.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
                    )
                    Text(text = stringResource(R.string.owner_stock_reconfirm_quantity))
                }
                Text(
                    text = stockReconfirmationState.product?.name.orEmpty(),
                    style = MangroTheme.typography.label.labelM,
                    color = MangroTheme.colors.textSubtitle,
                )
            }
        },
        actions = {
            MangroButton(
                text = stringResource(R.string.owner_stock_reconfirm_yes),
                onClick = { ownerMainViewModel.answerStockReconfirmation(true) },
                style = MangroButtonStyle.ACTIVE,
                enabled = !stockReconfirmationState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                textStyle = MangroTheme.typography.title.titleL,
            )
            MangroButton(
                text = stringResource(R.string.owner_stock_reconfirm_no),
                onClick = { ownerMainViewModel.answerStockReconfirmation(false) },
                style = MangroButtonStyle.DEFAULT,
                enabled = !stockReconfirmationState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                textStyle = MangroTheme.typography.title.titleL,
            )
            MangroButton(
                onClick = ownerMainViewModel::dismissStockReconfirmation,
                style = MangroButtonStyle.GHOST,
                enabled = !stockReconfirmationState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    text = stringResource(R.string.owner_stock_reconfirm_later),
                    style = MangroTheme.typography.body.bodyM.copy(fontSize = 14.sp, lineHeight = 19.6.sp),
                    textDecoration = TextDecoration.Underline,
                )
            }
        },
    )

    MangroDialogContainer(
        show = stockReconfirmationState.hasError,
        onDismissRequest = ownerMainViewModel::dismissStockReconfirmation,
        title = { Text(stringResource(R.string.owner_stock_reconfirm_error)) },
        actions = {
            MangroButton(
                text = stringResource(R.string.owner_management_retry),
                onClick = ownerMainViewModel::reloadStockReconfirmation,
                style = MangroButtonStyle.ACTIVE,
                modifier = Modifier.fillMaxWidth(),
            )
            MangroButton(
                text = stringResource(R.string.owner_stock_reconfirm_later),
                onClick = ownerMainViewModel::dismissStockReconfirmation,
                style = MangroButtonStyle.GHOST,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )

    NavHost(
        navController = navController,
        startDestination = OwnerHomeDestination,
        modifier = modifier,
    ) {
        ownerHomeNavGraph(
            refreshRequests = ownerMainViewModel.homeRefreshRequests,
            navigateToProducts = {
                navController.navigate(OwnerProductListDestination) {
                    popUpTo<OwnerHomeDestination> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            navigateToPickups = { completedOnly ->
                navController.navigateToOwnerPickups(
                    filter = if (completedOnly) ProductListFilter.COMPLETED else ProductListFilter.ALL,
                ) {
                    popUpTo<OwnerHomeDestination> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            navigateToSettings = {
                navController.navigate(OwnerSettingDestination) {
                    popUpTo<OwnerHomeDestination> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            navigateToProduct = { navController.navigate(OwnerProductDetailDestination(it)) },
            navigateToPickup = { navController.navigate(OwnerPickupDetailDestination(it)) },
            navigateToRegisterProduct = { navController.navigate(OwnerProductEditorDestination) },
        )

        ownerProductNavGraph(
            navController = navController,
            onCancelReservations = { navController.navigate(OwnerPickupCancellationDestination) },
            onPickupClick = { navController.navigate(OwnerPickupDetailDestination(it)) },
        )

        ownerSettingNavGraph(
            navigateToLogin = {
                onLogout()
                ownerMainViewModel.clearNotificationRequests()
            },
            navigateToPolicy = { navController.navigate(OwnerPolicyDestination(it)) { launchSingleTop = true } },
            navigateBack = { navController.popBackStack() },
        )

        ownerPickupNavGraph(
            navController = navController,
            navigateBack = { navController.popBackStack() },
            navigateToHome = { navController.popBackStack<OwnerHomeDestination>(inclusive = false) },
        )
    }
}
