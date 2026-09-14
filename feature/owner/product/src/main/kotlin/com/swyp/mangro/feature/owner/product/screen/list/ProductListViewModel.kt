package com.swyp.mangro.feature.owner.product.screen.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.feature.owner.product.data.OwnerPickupStore
import com.swyp.mangro.feature.owner.product.data.pickupTime
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.model.presentation
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Collections
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pickupStore: OwnerPickupStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProductListState(
            tab = savedStateHandle.get<String>(TAB)?.let(ProductListTab::valueOf) ?: ProductListTab.PRODUCTS,
            filter = savedStateHandle.get<String>(FILTER)?.let(ProductListFilter::valueOf) ?: ProductListFilter.ALL,
        ),
    )
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<ProductListEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(pickupStore.snapshot, pickupTime()) { snapshot, now -> snapshot.presentation(now) }
                .collect { pickups -> _uiState.update { it.copy(pickups = Collections.unmodifiableList(pickups)) } }
        }
    }

    fun updateContent(products: List<OwnerProductModel>) {
        pickupStore.updateProducts(products)
        _uiState.update { it.copy(products = Collections.unmodifiableList(products.toList())) }
    }

    fun handleAction(action: ProductListAction) {
        when (action) {
            is ProductListAction.TabSelected -> {
                savedStateHandle[TAB] = action.tab.name
                _uiState.update { it.copy(tab = action.tab) }
            }
            is ProductListAction.FilterSelected -> {
                savedStateHandle[FILTER] = action.filter.name
                _uiState.update { it.copy(filter = action.filter) }
            }
            is ProductListAction.ProductClicked -> send(ProductListEvent.OpenProduct(action.id))
            is ProductListAction.PickupClicked -> send(ProductListEvent.OpenPickup(action.id))
            is ProductListAction.PickupCompleteClicked -> {
                val completed = pickupStore.complete(action.id)
                _uiState.update { it.copy(hasPickupError = !completed) }
            }
            is ProductListAction.MenuSelected -> if (action.menu != OwnerMenu.STORE) send(ProductListEvent.OpenMenu(action.menu))
            ProductListAction.ReservationsCancelClicked -> send(ProductListEvent.CancelReservations(uiState.value.cancellationNeeded.map { it.productId }.distinct()))
        }
    }

    private fun send(event: ProductListEvent) {
        viewModelScope.launch { _event.send(event) }
    }

    private companion object {
        const val TAB = "store_tab"
        const val FILTER = "store_filter"
    }
}
