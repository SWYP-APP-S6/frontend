package com.swyp.mangro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.navigation.OwnerNavHost
import com.swyp.mangro.theme.MangroTheme

/** Local UI host until the catalog repository is connected. */
@Composable
internal fun MainScreen() {
    val viewModel: OwnerMainViewModel = hiltViewModel()
    val homePickups by viewModel.homePickups.collectAsStateWithLifecycle()
    var products by rememberSaveable { mutableStateOf(emptyList<OwnerProductModel>()) }
    MangroTheme {
        OwnerNavHost(
            products = products,
            homePickups = homePickups,
            onCompletePickup = viewModel::completePickup,
            storeClosingTime = "20:00",
            storeOpeningTime = "09:00",
            onSaveProducts = { changed ->
                val ids = changed.map { it.id }.toSet()
                products = products.filterNot { it.id in ids } + changed
            },
        )
    }
}
