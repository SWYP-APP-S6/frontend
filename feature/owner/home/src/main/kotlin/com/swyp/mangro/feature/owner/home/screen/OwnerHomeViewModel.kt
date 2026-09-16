package com.swyp.mangro.feature.owner.home.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.data.owner.home.repository.OwnerHomeRepository
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import com.swyp.mangro.data.user.repository.UserRepository
import com.swyp.mangro.feature.owner.home.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OwnerHomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val homeRepository: OwnerHomeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OwnerHomeUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<OwnerHomeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()
    private var refreshJob: Job? = null

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, canRegisterProduct = false) }
            val userRequest = async { userRepository.fetchMe().first() }
            val storeRequest = async { storeRepository.fetchMyStore().first() }
            val homeRequest = async { homeRepository.fetchHome().first() }
            val profile = userRequest.await().getOrNull()
            val store = storeRequest.await().getOrNull()
            val home = homeRequest.await().getOrNull()
            val validProfile = profile?.takeIf { it.role == "OWNER" }
            val validHome = home?.takeIf { it.storeId == store?.id }

            _uiState.update { state ->
                if (validProfile == null || store == null || validHome == null) {
                    state.copy(
                        isLoading = false,
                        errorMessage = R.string.owner_home_load_failed,
                        profile = validProfile,
                        approvalStatus = store?.status ?: StoreApprovalStatus.UNKNOWN,
                        canRegisterProduct = false,
                    )
                } else {
                    state.copy(
                        isLoading = false,
                        errorMessage = null,
                        profile = validProfile,
                        approvalStatus = if (store.canRegisterProduct) StoreApprovalStatus.from(validHome.storeStatus) else store.status,
                        canRegisterProduct = store.canRegisterProduct && StoreApprovalStatus.from(validHome.storeStatus) == StoreApprovalStatus.APPROVED,
                        hasRegisteredProduct = validHome.hasRegisteredProduct,
                        storeName = store.name,
                        storeCategory = store.categories.joinToString(" · ") { categoryLabel(it) },
                        expectedVisitCount = validHome.upcomingVisitCount,
                        completedPickupCount = validHome.completedTodayCount,
                        sellingCount = validHome.onSaleQty,
                        unreadNotificationCount = validHome.unreadNotificationCount,
                        attentionAvailable = false,
                        hasNewPickup = false,
                        visitors = validHome.upcomingVisits.map {
                            OwnerHomeVisitor(it.holdId.toString(), it.nickname, it.summary, it.totalQty, it.expiresAtMillis)
                        }.toPersistentList(),
                        products = validHome.products.map {
                            OwnerProduct(it.id.toString(), it.photoUrl, it.name, it.salePrice, it.availableQty, it.activeHoldQty, it.shortfallQty)
                        }.toPersistentList(),
                    )
                }
            }
        }
    }

    fun handleAction(action: OwnerHomeAction) {
        when (action) {
            OwnerHomeAction.Refresh -> refresh()

            is OwnerHomeAction.MarkAsPickedUp -> markAsPickedUp(action.pickupId)

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

            is OwnerHomeAction.ViewProduct,
            is OwnerHomeAction.ViewPickup,
            OwnerHomeAction.ViewPickups,
            OwnerHomeAction.ViewCompletedPickups,
            OwnerHomeAction.ViewNewPickups,
            OwnerHomeAction.ViewNotifications,
            OwnerHomeAction.ConfirmPickups,
            OwnerHomeAction.ViewCancellations,
            -> _event.trySend(OwnerHomeEvent.ShowMessage(R.string.owner_home_destination_unavailable))
        }
    }

    private fun markAsPickedUp(id: String) {
        val state = _uiState.value
        val visitor = state.visitors.firstOrNull { it.id == id } ?: return
        if (state.isLoading || state.errorMessage != null || id in state.completingPickupIds || visitor.pickupDeadlineMillis <= System.currentTimeMillis()) return
        val holdId = id.toLongOrNull() ?: return
        _uiState.update { it.copy(completingPickupIds = it.completingPickupIds.add(id)) }
        viewModelScope.launch {
            try {
                val result = homeRepository.markAsPickedUp(holdId).first()
                if (result.isSuccess) {
                    _uiState.update { it.copy(visitors = it.visitors.filterNot { visitor -> visitor.id == id }.toPersistentList()) }
                    _event.send(OwnerHomeEvent.ShowMessage(R.string.owner_home_pickup_completed))
                    refreshJob?.cancel()
                    refresh()
                } else {
                    _event.send(OwnerHomeEvent.ShowMessage(R.string.owner_home_pickup_failed))
                    refresh()
                }
            } finally {
                _uiState.update { it.copy(completingPickupIds = it.completingPickupIds.remove(id)) }
            }
        }
    }
}

private fun categoryLabel(category: String): String = when (category) {
    "VEGETABLE" -> "채소"
    "FRUIT" -> "과일"
    "MEAT" -> "육류"
    "SEAFOOD" -> "수산물"
    "DAIRY_EGG" -> "유제품/달걀"
    "BAKERY" -> "베이커리"
    "PREPARED_FOOD" -> "조리식품"
    else -> "기타"
}
