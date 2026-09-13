package com.swyp.mangro.feature.owner.product.screen.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel

@Composable
internal fun ProductDetailRoute(
    product: OwnerProductModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSave: (OwnerProductModel) -> Unit,
    onCancelReservations: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(product) {
        viewModel.updateProduct(product)
    }
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    is ProductDetailEvent.SaveProduct -> onSave(event.product)
                    ProductDetailEvent.NavigateBack -> onBack()
                    ProductDetailEvent.NavigateToEdit -> onEdit()
                    ProductDetailEvent.NavigateToCancellations -> onCancelReservations()
                }
            }
        }
    }
    ProductDetailScreen(uiState = uiState, onAction = viewModel::handleAction)
}
