package com.swyp.mangro.feature.owner.home.screen

import androidx.lifecycle.ViewModel
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class OwnerHomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(OwnerHomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<OwnerHomeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun updateProducts(products: PersistentList<OwnerProduct>) {
        _uiState.update { state ->
            state.copy(
                products = products,
                hasRegisteredProduct = products.isNotEmpty(),
                sellingCount = products.sumOf { it.remainingCount },
            )
        }
    }

    fun updatePickups(pickups: OwnerHomePickupState) {
        _uiState.update { state ->
            state.copy(
                storeName = pickups.storeName,
                expectedVisitCount = pickups.visitors.size,
                completedPickupCount = pickups.completedCount,
                cancellationRequiredCount = pickups.cancellationRequiredCount,
                hasNewPickup = pickups.hasNewPickup,
                needsPickupConfirmation = pickups.needsPickupConfirmation,
                visitors = pickups.visitors,
            )
        }
    }

    fun handleAction(action: OwnerHomeAction) {
        when (action) {
            OwnerHomeAction.RegisterStore -> _event.trySend(OwnerHomeEvent.NavigateToRegisterStore)
            is OwnerHomeAction.CompletePickup -> {
                _event.trySend(OwnerHomeEvent.CompletePickup(action.pickupId))
            }

            OwnerHomeAction.ConfirmPickups -> {
                _event.trySend(OwnerHomeEvent.NavigateToExpiredPickups)
            }

            OwnerHomeAction.DismissAttention -> {
                _uiState.update { it.copy(isAttentionDismissed = true) }
            }

            OwnerHomeAction.RegisterProduct -> {
                _event.trySend(OwnerHomeEvent.NavigateToRegisterProduct)
            }

            OwnerHomeAction.ViewCancellations -> {
                _event.trySend(OwnerHomeEvent.NavigateToCancellations)
            }

            OwnerHomeAction.ViewCompletedPickups -> {
                _event.trySend(OwnerHomeEvent.NavigateToCompletedPickups)
            }

            OwnerHomeAction.ViewNewPickups -> {
                _event.trySend(OwnerHomeEvent.NavigateToPickups)
            }

            OwnerHomeAction.ViewNotifications -> {
                _event.trySend(OwnerHomeEvent.ShowNotificationsPreparing)
            }

            is OwnerHomeAction.ViewPickup -> {
                _event.trySend(OwnerHomeEvent.NavigateToPickup(action.pickupId))
            }

            OwnerHomeAction.ViewPickups -> {
                _event.trySend(OwnerHomeEvent.NavigateToPickups)
            }

            is OwnerHomeAction.ViewProduct -> {
                _event.trySend(OwnerHomeEvent.NavigateToProduct(action.productId))
            }

            OwnerHomeAction.ViewSettings -> {
                _event.trySend(OwnerHomeEvent.NavigateToSettings)
            }

            OwnerHomeAction.ViewStore -> _event.trySend(OwnerHomeEvent.NavigateToStore)

            OwnerHomeAction.ViewProducts -> {
                _event.trySend(OwnerHomeEvent.NavigateToProducts)
            }
        }
    }
}
