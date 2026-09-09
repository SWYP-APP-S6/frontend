package com.swyp.mangro.feature.owner.home.screen

import androidx.lifecycle.ViewModel
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.feature.owner.home.R
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeAction
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeEvent
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeUiState
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeVisitor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
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

    // TODO - 초기화
    init {
        _uiState.update {
            it.copy(
                hasRegisteredProduct = true,
                storeName = "청과마을",
                storeCategory = "과채류",
                expectedVisitCount = 10,
                completedPickupCount = 8,
                sellingCount = 20,
                hasNewPickup = true,
                cancellationRequiredCount = 2,
                needsPickupConfirmation = true,
                visitors = persistentListOf(
                    OwnerHomeVisitor("pickup-1", "윤지현", "시금치 한 단", 1, System.currentTimeMillis() + 8 * 60_000),
                    OwnerHomeVisitor("pickup-2", "닉네임최대몇글자까지", "상품명은25자내외까지허용입니다그것을넘어가면안돼", 1, System.currentTimeMillis() + 8 * 60_000),
                    OwnerHomeVisitor("pickup-3", "망그로", "콩나물 한 바구니", 2, System.currentTimeMillis() + 12 * 60_000),
                ),
                products = persistentListOf(
                    OwnerProduct("product-1", "android.resource://com.swyp.mangro.feature.owner.home/${R.drawable.sample_spinach}", "시금치 한 단", 4_000, 6, 4, 2),
                    OwnerProduct("product-2", "android.resource://com.swyp.mangro.feature.owner.home/${R.drawable.sample_zucchini}", "애호박", 4_000, 2, 3, 0),
                    OwnerProduct("product-3", "android.resource://com.swyp.mangro.feature.owner.home/${R.drawable.sample_sprouts}", "콩나물 한 바구니", 4_000, 4, 3, 2),
                ),
            )
        }
    }

    fun handleAction(action: OwnerHomeAction) {
        when (action) {
            is OwnerHomeAction.CompletePickup -> {
            }

            OwnerHomeAction.ConfirmPickups -> {
            }

            OwnerHomeAction.DismissAttention -> {
            }

            OwnerHomeAction.RegisterProduct -> {
            }

            OwnerHomeAction.ViewCancellations -> {
            }

            OwnerHomeAction.ViewCompletedPickups -> {
            }

            OwnerHomeAction.ViewNewPickups -> {
            }

            OwnerHomeAction.ViewNotifications -> {
            }

            is OwnerHomeAction.ViewPickup -> {
            }

            OwnerHomeAction.ViewPickups -> {
            }

            is OwnerHomeAction.ViewProduct -> {
            }

            OwnerHomeAction.ViewProducts -> {
            }
        }
    }
}
