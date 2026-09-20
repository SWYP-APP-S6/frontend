package com.swyp.mangro.feature.consumer.hold.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishHistoryItem
import com.swyp.mangro.core.designsystem.component.card.wishlist.WishStatus
import com.swyp.mangro.data.consumer.hold.model.HoldSummary
import com.swyp.mangro.data.consumer.hold.repository.HoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HoldHistoryViewModel @Inject constructor(
    private val repository: HoldRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HoldHistoryUiState())
    val uiState: StateFlow<HoldHistoryUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HoldHistoryUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadHoldHistory()
    }

    fun refresh() {
        loadHoldHistory()
    }

    fun handleAction(action: HoldHistoryUiAction) {
        when (action) {
            is HoldHistoryUiAction.OnItemClick -> {
                viewModelScope.launch {
                    _uiEvent.send(HoldHistoryUiEvent.NavigateToHoldDetail(action.id))
                }
            }

            is HoldHistoryUiAction.OnMenuClick -> {
                viewModelScope.launch {
                    _uiEvent.send(HoldHistoryUiEvent.NavigateToMenu(action.menu))
                }
            }
        }
    }

    private fun loadHoldHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.fetchHolds().collect { result ->
                result
                    .onSuccess { history ->
                        val (inProgress, past) = history.holds.partition { it.status == "HOLDING" }
                        _uiState.update {
                            it.copy(
                                inProgressItems = inProgress.map { hold -> hold.toWishHistoryItem() }.toPersistentList(),
                                pastItems = past.map { hold -> hold.toWishHistoryItem() }.toPersistentList(),
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

private fun HoldSummary.toWishHistoryItem(): WishHistoryItem = WishHistoryItem(
    id = id.toString(),
    imageUrl = photoUrl,
    discountRate = null,
    name = productName,
    quantity = qty,
    storeName = storeName,
    price = totalPrice,
    status = status.toWishStatus(),
    dateLabel = if (status != "HOLDING") heldAtMillis.toDateLabel() else null,
    requestTimeMillis = if (status == "HOLDING") heldAtMillis else null,
    endTimeMillis = if (status == "HOLDING") expiresAtMillis else null,
)

private fun String.toWishStatus(): WishStatus = when (this) {
    "HOLDING" -> WishStatus.IN_PROGRESS
    "COMPLETED" -> WishStatus.PICKED_UP
    "CANCELED", "EXPIRED" -> WishStatus.EXPIRED
    else -> WishStatus.EXPIRED
}

private fun Long.toDateLabel(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("MM/dd"))
