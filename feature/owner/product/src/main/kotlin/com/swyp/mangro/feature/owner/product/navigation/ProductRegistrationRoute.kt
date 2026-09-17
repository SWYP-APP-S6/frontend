package com.swyp.mangro.feature.owner.product.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.theme.MangroTheme
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.screen.editor.ProductRegistrationViewModel

@Composable
internal fun ProductRegistrationRoute(
    onBack: () -> Unit,
    onSave: (OwnerProductModel) -> Unit,
    viewModel: ProductRegistrationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    LaunchedEffect(viewModel) { viewModel.save.collect { onSave(it) } }
    if (state.registrationFailed) {
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            text = { Text(stringResource(R.string.product_registration_failed)) },
            confirmButton = {
                MangroButton(text = stringResource(R.string.product_registration_confirm), onClick = viewModel::dismissError, style = MangroButtonStyle.TEXT)
            },
        )
    }
    Box(Modifier.fillMaxSize()) {
        val store = state.store
        if (store?.canRegisterProduct == true) {
            ProductEditorNavHost(
                storeCategory = store.categories.singleOrNull(),
                storeOpeningTime = store.businessOpenTime.take(5),
                storeClosingTime = store.businessCloseTime.take(5),
                onBack = onBack,
                onSave = viewModel::submit,
            )
        } else if (!state.isChecking) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
                Text(
                    stringResource(
                        when (store?.status) {
                            StoreApprovalStatus.PENDING -> R.string.product_registration_pending
                            StoreApprovalStatus.REJECTED -> R.string.product_registration_rejected
                            else -> R.string.product_registration_check_failed
                        },
                    ),
                )
                MangroButton(text = stringResource(R.string.product_registration_retry), onClick = viewModel::refresh, style = MangroButtonStyle.OUTLINED)
                MangroButton(text = stringResource(R.string.product_registration_back), onClick = onBack, style = MangroButtonStyle.TEXT)
            }
        }
        if (state.isChecking || state.isSubmitting) {
            Box(
                Modifier.fillMaxSize().background(MangroTheme.colors.surfaceNormal.copy(alpha = 0.9f)).clickable(onClick = {}),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = MangroTheme.colors.primaryNormal) }
        }
    }
    BackHandler(enabled = state.isSubmitting || state.isChecking) {}
}
