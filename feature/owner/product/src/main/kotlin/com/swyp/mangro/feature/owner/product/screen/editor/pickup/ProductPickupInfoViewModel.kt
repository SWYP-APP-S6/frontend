package com.swyp.mangro.feature.owner.product.screen.editor.pickup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.swyp.mangro.feature.owner.product.model.OwnerProductModel
import com.swyp.mangro.feature.owner.product.model.ProductDraftModel
import com.swyp.mangro.feature.owner.product.util.addProductTag
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
            update(ProductPickupInfoState(isLoading = false, product = draft, storeClosingTime = storeClosingTime, storeOpeningTime = storeOpeningTime, storeCategory = storeCategory, tags = draft.tags))
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
            is ProductPickupInfoAction.TagChanged -> update(state.copy(tagInput = action.value))
            is ProductPickupInfoAction.TagRemoveClicked -> update(state.copy(tags = state.tags - action.value))
            ProductPickupInfoAction.TagSubmitted -> if (state.canAddTag) update(state.copy(tags = addProductTag(state.tags, state.tagInput), tagInput = ""))
            ProductPickupInfoAction.RegisterClicked -> if (state.canPreview) update(state.copy(tags = addProductTag(state.tags, state.tagInput), tagInput = "", showPreview = true))
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
        savedStateHandle[STATE] = state.copy(tags = Collections.unmodifiableList(state.tags.toList()), pickupTimeOptions = Collections.unmodifiableList(state.pickupTimeOptions.toList()))
    }
    private companion object {
        const val STATE = "pickupState"
    }
}
