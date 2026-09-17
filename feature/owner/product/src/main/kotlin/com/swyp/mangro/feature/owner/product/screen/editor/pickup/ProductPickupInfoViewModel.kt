package com.swyp.mangro.feature.owner.product.screen.editor.pickup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.model.ProductDraftModel
import com.swyp.mangro.feature.owner.product.util.pickupTimeOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalTime
import java.time.ZoneId
import java.util.Collections
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class ProductPickupInfoViewModel internal constructor(private val savedStateHandle: SavedStateHandle, private val clock: Clock) : ViewModel() {
    @Inject constructor(savedStateHandle: SavedStateHandle) : this(savedStateHandle, Clock.system(ZoneId.of("Asia/Seoul")))
    val uiState = savedStateHandle.getStateFlow(STATE, ProductPickupInfoState())
    private val _event = Channel<ProductPickupInfoEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun initialize(input: ProductDraftModel, storeClosingTime: String, storeOpeningTime: String, storeCategory: String? = null) {
        val draft = OwnerProductModel(
            id = input.id,
            name = input.name,
            photos = input.photos,
            originalPrice = input.originalPrice,
            salePrice = input.salePrice,
            initialQuantity = input.quantity,
            remainingQuantity = input.quantity,
            pickupEndTime = storeClosingTime,
        )
        if (uiState.value.product != null) {
            update(uiState.value.copy(product = draft, storeClosingTime = storeClosingTime, storeOpeningTime = storeOpeningTime, storeCategory = storeCategory))
        } else {
            update(ProductPickupInfoState(isLoading = false, product = draft, storeClosingTime = storeClosingTime, storeOpeningTime = storeOpeningTime, storeCategory = storeCategory))
        }
        refreshTimeOptions()
    }

    fun refreshTimeOptions() {
        val state = uiState.value
        val options = pickupTimeOptions(state.storeOpeningTime, state.storeClosingTime, LocalTime.now(clock))
        update(state.copy(pickupTimeOptions = options, showPreview = state.showPreview && (state.pickupTime ?: state.storeClosingTime) in options))
    }

    fun handleAction(action: ProductPickupInfoAction) {
        refreshTimeOptions()
        val state = uiState.value
        when (action) {
            is ProductPickupInfoAction.PickupTimeChanged -> if (action.value in state.pickupTimeOptions) update(state.copy(pickupTime = action.value))
            ProductPickupInfoAction.RegisterClicked -> if (state.canPreview) update(state.copy(showPreview = true))
            ProductPickupInfoAction.PreviewDismissed -> update(state.copy(showPreview = false))
            ProductPickupInfoAction.EditBasicInfoClicked -> {
                val next = state.copy(showPreview = false)
                update(next)
                _event.trySend(ProductPickupInfoEvent.EditBasicInfo)
            }
            ProductPickupInfoAction.NavigationBackClicked -> _event.trySend(ProductPickupInfoEvent.Back)
            ProductPickupInfoAction.SaveClicked -> if (state.showPreview && state.canPreview && state.draft != null) {
                update(state.copy(showPreview = false))
                _event.trySend(ProductPickupInfoEvent.Save(checkNotNull(state.draft)))
            }
        }
    }

    private fun update(state: ProductPickupInfoState) {
        savedStateHandle[STATE] = state.copy(pickupTimeOptions = Collections.unmodifiableList(state.pickupTimeOptions.toList()))
    }
    private companion object {
        const val STATE = "pickupState"
    }
}
