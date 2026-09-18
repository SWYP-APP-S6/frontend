package com.swyp.mangro.feature.consumer.hold.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishDetailItem
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishStatus
import com.swyp.mangro.data.consumer.hold.model.HoldDetail
import com.swyp.mangro.data.consumer.hold.repository.HoldRepository
import com.swyp.mangro.feature.consumer.hold.navigation.HoldDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HoldDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HoldRepository,
) : ViewModel() {
    private val holdId: Long = savedStateHandle.toRoute<HoldDetailDestination>().holdId.toLong()

    private val _uiState = MutableStateFlow(HoldDetailUiState())
    val uiState: StateFlow<HoldDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HoldDetailUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadHoldDetail(holdId)
    }

    fun handleAction(action: HoldDetailUiAction) {
        when (action) {
            is HoldDetailUiAction.OnCancelClick -> {
                viewModelScope.launch {
                    repository.cancelHold(holdId).collect { result ->
                        result
                            .onSuccess { detail ->
                                _uiState.update {
                                    it.copy(
                                        detail = detail.toHoldDetailInfo(),
                                        isCancelButtonEnabled = false,
                                    )
                                }
                                _uiEvent.send(HoldDetailUiEvent.NavigateToHoldHistory)
                            }
                            .onFailure {
                                // TODO: 실패 처리
                            }
                    }
                }
            }
        }
    }

    private fun loadHoldDetail(holdId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.fetchHold(holdId).collect { result ->
                result
                    .onSuccess { detail ->
                        _uiState.update {
                            it.copy(
                                detail = detail.toHoldDetailInfo(),
                                isCancelButtonEnabled = detail.status == "HOLDING",
                                isLoading = false,
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                    }
            }
        }
    }
}

private fun HoldDetail.toHoldDetailInfo(): HoldDetailInfo {
    val firstItem = items.firstOrNull()
    return HoldDetailInfo(
        item = WishDetailItem(
            id = id.toString(),
            name = firstItem?.name.orEmpty(),
            quantity = totalQty,
            price = totalPrice,
            originalPrice = firstItem?.originalPrice,
            discountRate = firstItem?.discountRate,
            status = status.toWishStatus(),
        ),
        images = firstItem?.photoUrl?.let { persistentListOf(it) } ?: persistentListOf(),
        storeName = store.name,
        requestedDateText = heldAtMillis.toDateLabel(),
        requestedTimeText = heldAtMillis.toTimeLabel(),
        expiredDateText = expiresAtMillis.toDateLabel(),
        expiredTimeText = expiresAtMillis.toTimeLabel(),
    )
}

private fun String.toWishStatus(): WishStatus = when (this) {
    "HOLDING" -> WishStatus.IN_PROGRESS
    "COMPLETED" -> WishStatus.PICKED_UP
    "CANCELED", "EXPIRED" -> WishStatus.EXPIRED
    else -> WishStatus.EXPIRED
}

private fun Long.toDateLabel(): String = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd"))

private fun Long.toTimeLabel(): String = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
