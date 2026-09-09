package com.swyp.mangro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroButton
import com.swyp.mangro.core.designsystem.component.MangroButtonStyle
import com.swyp.mangro.feature.owner.product.OwnerProduct
import com.swyp.mangro.feature.owner.product.OwnerProductFlow
import com.swyp.mangro.theme.MangroTheme

/** Local UI host until the catalog and owner:pickup navigation are connected. */
@Composable
internal fun MainScreen() {
    var products by rememberSaveable { mutableStateOf(emptyList<OwnerProduct>()) }
    var cancellationProductIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    MangroTheme {
        if (cancellationProductIds.isEmpty()) {
            OwnerProductFlow(
                products = products,
                storeClosingTime = "20:00",
                onSaveProducts = { changed ->
                    val ids = changed.map { it.id }.toSet()
                    products = products.filterNot { it.id in ids } + changed
                },
                onCancelReservations = { cancellationProductIds = it },
            )
        } else {
            BackHandler { cancellationProductIds = emptyList() }
            Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("찜 취소하기")
                products.filter { it.id in cancellationProductIds }.forEach {
                    Text("${it.name} · 부족 수량 ${it.shortageQuantity}개")
                }
                Text("찜 내역을 불러올 수 없어요. 잠시 후 다시 확인해주세요.")
                MangroButton("돌아가기", { cancellationProductIds = emptyList() }, MangroButtonStyle.ACTIVE)
            }
        }
    }
}
