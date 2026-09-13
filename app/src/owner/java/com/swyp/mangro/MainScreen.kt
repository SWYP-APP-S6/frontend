package com.swyp.mangro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.navigation.OwnerNavHost
import com.swyp.mangro.theme.MangroTheme

/** Local UI host until the catalog repository is connected. */
@Composable
internal fun MainScreen() {
    var products by rememberSaveable { mutableStateOf(emptyList<OwnerProductModel>()) }
    MangroTheme {
        OwnerNavHost(
            products = products,
            storeClosingTime = "20:00",
            onSaveProducts = { changed ->
                val ids = changed.map { it.id }.toSet()
                products = products.filterNot { it.id in ids } + changed
            },
        )
    }
}
