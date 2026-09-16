package com.swyp.mangro.feature.owner.product.screen.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.appbar.OwnerMenu
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedHold
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import com.swyp.mangro.feature.owner.product.model.presentation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    private var holds = emptyList<ManagedHold>()
    private var offset = 0L
    private var mutationInProgress = false
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            while (true) {
                presentHolds()
                delay(1000)
            }
        }
    }

    fun refresh() {
        if (uiState.value.tab != ProductListTab.PICKUPS || uiState.value.isLoading || mutationInProgress) return
        val status = when (uiState.value.filter) {
            ProductListFilter.ALL -> null
            ProductListFilter.COMPLETED -> HoldStatus.COMPLETED
            ProductListFilter.UNAVAILABLE -> HoldStatus.CANCELED_BY_OWNER
            ProductListFilter.CANCELLED -> HoldStatus.CANCELED_BY_USER
            ProductListFilter.EXPIRED -> HoldStatus.EXPIRED
        }
        _uiState.update { it.copy(isLoading = true, hasPickupError = false) }
        refreshJob = viewModelScope.launch {
            val all = mutableListOf<ManagedHold>()
            var page = 0
            var failed = false
            var total = 0L
            while (true) {
                val result = repository.fetchHolds(page, status).first()
                val value = result.getOrNull()
                if (value == null) {
                    failed = true
                    break
                }
                if (!value.last && value.holds.all { next -> all.any { it.id == next.id } }) {
                    failed = true
                    break
                }
                all.addAll(value.holds)
                total = value.total
                offset = value.serverTime - System.currentTimeMillis()
                if (value.last) break
                if (value.holds.isEmpty()) {
                    failed = true
                    break
                }
                page++
            }
            if (!failed) holds = all.distinctBy { it.id }
            _uiState.update {
                it.copy(
                    totalHolds = if (failed) it.totalHolds else total,
                    isLoading = false,
                    hasPickupError = failed,
                )
            }
            presentHolds()
        }
    }

    private fun presentHolds() {
        _uiState.update { state -> state.copy(pickups = holds.map { it.presentation(System.currentTimeMillis() + offset, offset, mutationInProgress || state.isLoading || state.hasPickupError) }) }
    }

    fun handleAction(action: ProductListAction) {
        when (action) {
            ProductListAction.Refresh -> refresh()
            is ProductListAction.TabSelected -> {
                savedStateHandle["store_tab"] = action.tab.name
                _uiState.update { it.copy(tab = action.tab) }
                refresh()
            }
            is ProductListAction.FilterSelected -> {
                savedStateHandle["store_filter"] = action.filter.name
                refreshJob?.cancel()
                holds = emptyList()
                _uiState.update { it.copy(filter = action.filter, pickups = emptyList(), isLoading = false, hasPickupError = false) }
                refresh()
            }
            is ProductListAction.ProductClicked -> send(ProductListEvent.OpenProduct(action.id))
            is ProductListAction.PickupClicked -> send(ProductListEvent.OpenPickup(action.id))
            is ProductListAction.PickupCompleteClicked -> markAsPickedUp(action.id)
            is ProductListAction.MenuSelected -> if (action.menu != OwnerMenu.STORE) send(ProductListEvent.OpenMenu(action.menu))
            ProductListAction.ReservationsCancelClicked -> send(ProductListEvent.CancelReservations(emptyList()))
        }
    }

    private fun markAsPickedUp(id: String) {
        presentHolds()
        if (mutationInProgress || uiState.value.pickups.none { it.request.id == id && it.canComplete }) return
        mutationInProgress = true
        presentHolds()
        viewModelScope.launch {
            val result = repository.markAsPickedUp(id.toLong()).first()
            mutationInProgress = false
            if (result.isSuccess) {
                refresh()
            } else {
                _uiState.update { it.copy(hasPickupError = true) }
                presentHolds()
            }
        }
    }
    private fun send(event: ProductListEvent) {
        _event.trySend(event)
    }
}
