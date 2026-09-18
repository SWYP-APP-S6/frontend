package com.swyp.mangro.feature.owner.product.screen.reconfirmation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.core.designsystem.component.dialog.MangroDialogContainer
import com.swyp.mangro.feature.owner.product.R
import com.swyp.mangro.feature.owner.product.component.StockReconfirmationDialog

@Composable
fun StockReconfirmationHost(
    requestKey: String?,
    productId: Long?,
    onEditProduct: (String) -> Unit,
    viewModel: StockReconfirmationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(requestKey, productId) {
        if (requestKey != null && productId != null) viewModel.open(requestKey, productId)
    }
    LaunchedEffect(viewModel) { viewModel.editProduct.collect { onEditProduct(it.toString()) } }
    state.product?.let {
        StockReconfirmationDialog(it.name, it.quantity, state.isSaving, { viewModel.answer(true) }, { viewModel.answer(false) }, viewModel::dismiss)
    }
    MangroDialogContainer(
        show = state.hasError,
        onDismissRequest = viewModel::dismiss,
        title = { Text(stringResource(R.string.owner_stock_reconfirm_error)) },
        actions = {
            MangroButton(stringResource(R.string.owner_management_retry), viewModel::reload, MangroButtonStyle.ACTIVE, Modifier.fillMaxWidth())
            MangroButton(stringResource(R.string.owner_stock_reconfirm_later), viewModel::dismiss, MangroButtonStyle.GHOST, Modifier.fillMaxWidth())
        },
    )
}
