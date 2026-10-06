package com.swyp.mangro.feature.owner.home.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.owner.home.repository.OwnerHomeRepository
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.feature.owner.home.R
import com.swyp.mangro.feature.owner.home.mapper.toCategories
import com.swyp.mangro.feature.owner.home.mapper.toOwnerHomeVisitor
import com.swyp.mangro.feature.owner.home.mapper.toOwnerProducts
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeAction
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeEvent
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OwnerHomeViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val homeRepository: OwnerHomeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OwnerHomeUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<OwnerHomeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private var refreshJob: Job? = null
    private var notificationJob: Job? = null
    private val initialLoad: Job

    init {
        initialLoad = viewModelScope.launch {
            combine(
                storeRepository.fetchMyStoreInformation(),
                homeRepository.fetchHome(),
            ) { store, home ->
                val homeResult = home.getOrNull()?.takeIf { it.storeId == store.id } ?: throw IllegalStateException()

                OwnerHomeUiState(
                    approvalStatus = if (store.canRegisterProduct) {
                        StoreApprovalStatus.from(homeResult.storeStatus)
                    } else {
                        store.status
                    },
                    canRegisterProduct = store.canRegisterProduct && StoreApprovalStatus.from(homeResult.storeStatus) == StoreApprovalStatus.APPROVED,
                    hasRegisteredProduct = homeResult.hasRegisteredProduct,
                    storeName = store.name,
                    storeCategory = store.categories.toCategories(),
                    expectedVisitCount = homeResult.upcomingVisitCount,
                    completedPickupCount = homeResult.completedTodayCount,
                    sellingCount = homeResult.onSaleProductCount,
                    unreadNotificationCount = homeResult.unreadNotificationCount,
                    visitors = homeResult.upcomingVisits.toOwnerHomeVisitor(),
                    products = homeResult.products.toOwnerProducts(),
                )
            }.catch {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = R.string.owner_home_load_failed,
                        canRegisterProduct = false,
                    )
                }
            }.collect {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = null,
                        approvalStatus = it.approvalStatus,
                        canRegisterProduct = it.canRegisterProduct,
                        hasRegisteredProduct = it.hasRegisteredProduct,
                        storeName = it.storeName,
                        storeCategory = it.storeCategory,
                        expectedVisitCount = it.expectedVisitCount,
                        completedPickupCount = it.completedPickupCount,
                        completingPickupIds = it.completingPickupIds,
                        sellingCount = it.sellingCount,
                        unreadNotificationCount = it.unreadNotificationCount,
                        visitors = it.visitors,
                        products = it.products,
                    )
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    fun observeRefreshRequests(requests: Flow<Unit>) {
        if (notificationJob?.isActive == true) return
        notificationJob = viewModelScope.launch {
            requests.debounce(500.milliseconds).conflate().collect {
                initialLoad.join()
                refreshJob?.join()
                startRefresh(showLoading = false).join()
            }
        }
    }

    fun refresh() {
        startRefresh(showLoading = true)
    }

    private fun startRefresh(showLoading: Boolean): Job {
        refreshJob?.takeIf { it.isActive }?.let { return it }

        return viewModelScope.launch {
            homeRepository.fetchHome()
                .onStart {
                    if (showLoading) _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }.catch {
                    if (showLoading) _uiState.update { it.copy(isLoading = false, errorMessage = R.string.owner_home_load_failed) }
                }.onCompletion {
                    _uiState.update { it.copy(isLoading = false) }
                }.collect { result ->
                    val home = result.getOrNull() ?: return@collect
                    val approvalStatus = StoreApprovalStatus.from(home.storeStatus)

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = null,
                            approvalStatus = approvalStatus,
                            canRegisterProduct = approvalStatus == StoreApprovalStatus.APPROVED,
                            hasRegisteredProduct = home.hasRegisteredProduct,
                            expectedVisitCount = home.upcomingVisitCount,
                            completedPickupCount = home.completedTodayCount,
                            sellingCount = home.onSaleProductCount,
                            unreadNotificationCount = home.unreadNotificationCount,
                            visitors = home.upcomingVisits.toOwnerHomeVisitor(),
                            products = home.products.toOwnerProducts(),
                        )
                    }
                }
        }.also { refreshJob = it }
    }

    fun handleAction(action: OwnerHomeAction) {
        when (action) {
            OwnerHomeAction.Refresh -> refresh()

            is OwnerHomeAction.MarkAsPickedUp -> markAsPickUpProduct(action.id)

            OwnerHomeAction.DismissAttention -> _uiState.update { it.copy(isAttentionDismissed = true) }

            OwnerHomeAction.RegisterProduct -> {
                if (_uiState.value.canRegisterProduct && !_uiState.value.isLoading) {
                    _event.trySend(OwnerHomeEvent.NavigateToRegisterProduct)
                } else {
                    _event.trySend(OwnerHomeEvent.ShowMessage(R.string.owner_home_approval_required))
                }
            }

            OwnerHomeAction.ViewSettings -> _event.trySend(OwnerHomeEvent.NavigateToSettings)

            OwnerHomeAction.ViewProducts -> _event.trySend(OwnerHomeEvent.NavigateToProducts)

            is OwnerHomeAction.ViewProduct -> _event.trySend(OwnerHomeEvent.NavigateToProduct(action.productId))

            OwnerHomeAction.ViewPickups -> _event.trySend(OwnerHomeEvent.NavigateToPickups(completedOnly = false))

            OwnerHomeAction.ViewCompletedPickups -> _event.trySend(OwnerHomeEvent.NavigateToPickups(completedOnly = true))

            is OwnerHomeAction.ViewPickup -> _event.trySend(OwnerHomeEvent.NavigateToPickup(action.id))

            OwnerHomeAction.ViewNewPickups,
            OwnerHomeAction.ViewNotifications,
            OwnerHomeAction.ConfirmPickups,
            OwnerHomeAction.ViewCancellations,
            -> _event.trySend(OwnerHomeEvent.ShowMessage(R.string.owner_home_destination_unavailable))
        }
    }

    private fun markAsPickUpProduct(id: String) {
        val holdId = id.toLongOrNull() ?: return

        _uiState.update { before -> before.copy(completingPickupIds = before.completingPickupIds.add(id)) }

        viewModelScope.launch {
            homeRepository
                .markAsPickedUp(holdId)
                .catch { _ ->
                    _event.trySend(OwnerHomeEvent.ShowMessage(R.string.owner_home_pickup_failed))
                }.onCompletion {
                    _uiState.update { it.copy(completingPickupIds = it.completingPickupIds.remove(id)) }
                }.collect {
                    refresh()
                    _uiState.update { it.copy(visitors = it.visitors.filterNot { visitor -> visitor.id == id }.toPersistentList()) }
                    _event.trySend(OwnerHomeEvent.ShowMessage(R.string.owner_home_pickup_completed))
                }
        }
    }
}
