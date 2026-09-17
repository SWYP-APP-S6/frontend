package com.swyp.mangro.feature.owner.product.screen.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import com.swyp.mangro.feature.owner.product.model.OwnerPickupModel
import com.swyp.mangro.feature.owner.product.model.presentation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: OwnerProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProductListState(
            tab = savedStateHandle.get<String>("store_tab")?.let(ProductListTab::valueOf) ?: ProductListTab.PRODUCTS,
            filter = savedStateHandle.get<String>("store_filter")?.let(ProductListFilter::valueOf) ?: ProductListFilter.ALL,
        ),
    )
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<ProductListEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    val pickups = uiState.map { it.filter }.distinctUntilChanged().flatMapLatest { filter ->
        val status = when (filter) {
            ProductListFilter.ALL -> null
            ProductListFilter.COMPLETED -> HoldStatus.COMPLETED
            ProductListFilter.UNAVAILABLE -> HoldStatus.CANCELED_BY_OWNER
            ProductListFilter.CANCELLED -> HoldStatus.CANCELED_BY_USER
            ProductListFilter.EXPIRED -> HoldStatus.EXPIRED
        }
        var offset = 0L

        repository.pagedHolds(status) { page ->
            offset = page.serverTime - System.currentTimeMillis()
            _uiState.update {
                if (it.filter == filter) it.copy(totalHolds = page.total, filteredTotal = page.filteredTotal) else it
            }
        }.map { data ->
            data.map { it.presentation(System.currentTimeMillis() + offset, offset, false) }
        }
    }.cachedIn(viewModelScope)

    fun openPickups(filter: ProductListFilter) {
        savedStateHandle["store_tab"] = ProductListTab.PICKUPS.name
        savedStateHandle["store_filter"] = filter.name
        _uiState.update {
            it.copy(
                tab = ProductListTab.PICKUPS,
                filter = filter,
                filteredTotal = if (it.filter == filter) it.filteredTotal else 0,
                hasPickupError = false,
            )
        }
    }

    fun refresh() {
        _uiState.update { it.copy(hasPickupError = false) }
        repository.refreshHolds()
    }

    fun handleAction(action: ProductListAction) {
        when (action) {
            ProductListAction.Refresh -> refresh()

            is ProductListAction.TabSelected -> {
                savedStateHandle["store_tab"] = action.tab.name
                _uiState.update { it.copy(tab = action.tab) }
            }

            is ProductListAction.FilterSelected -> {
                if (action.filter == uiState.value.filter) return
                savedStateHandle["store_filter"] = action.filter.name
                _uiState.update { it.copy(filter = action.filter, filteredTotal = 0, hasPickupError = false) }
            }

            is ProductListAction.ProductClicked -> send(ProductListEvent.OpenProduct(action.id))

            is ProductListAction.PickupClicked -> send(ProductListEvent.OpenPickup(action.id))

            is ProductListAction.PickupCompleteClicked -> markAsPickedUp(action.pickup)

            is ProductListAction.MenuSelected -> if (action.menu != OwnerMenu.STORE) send(ProductListEvent.OpenMenu(action.menu))

            ProductListAction.ReservationsCancelClicked -> send(ProductListEvent.CancelReservations(emptyList()))
        }
    }

    private fun markAsPickedUp(pickup: OwnerPickupModel) {
        if (uiState.value.mutationInProgress ||
            uiState.value.hasPickupError ||
            !pickup.canComplete ||
            pickup.request.endTimeMillis?.let { it <= System.currentTimeMillis() } != false
        ) {
            return
        }
        _uiState.update { it.copy(mutationInProgress = true) }
        viewModelScope.launch {
            val result = repository.markAsPickedUp(pickup.request.id.toLong()).first()
            _uiState.update { it.copy(mutationInProgress = false, hasPickupError = result.isFailure) }
            if (result.isSuccess) refresh()
        }
    }
    private fun send(event: ProductListEvent) {
        _event.trySend(event)
    }
}
