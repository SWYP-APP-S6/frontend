package com.swyp.mangro.feature.owner.product.screen.editor.price

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.swyp.mangro.feature.owner.product.model.ProductDraftModel
import com.swyp.mangro.feature.owner.product.util.parsePrice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class ProductPriceViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val uiState = savedStateHandle.getStateFlow(STATE, ProductPriceState())
    private val _event = Channel<ProductPriceEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun initialize(draft: ProductDraftModel) {
        savedStateHandle["draft"] = draft

        if (savedStateHandle.get<Boolean>("initialized") == true) return
        savedStateHandle["initialized"] = true
    }

    fun handleAction(action: ProductPriceAction) {
        val state = uiState.value
        when (action) {
            is ProductPriceAction.OriginalPriceChanged -> update(state.copy(originalPrice = action.value))

            is ProductPriceAction.SalePriceChanged -> update(state.copy(salePrice = action.value))

            is ProductPriceAction.QuantityChanged -> if (action.value > 0) update(state.copy(quantity = action.value))

            ProductPriceAction.NextClicked -> {
                val originalPrice = parsePrice(state.originalPrice)
                val salePrice = parsePrice(state.salePrice)
                if (state.canContinue && originalPrice != null && salePrice != null) {
                    _event.trySend(
                        ProductPriceEvent.Next(
                            checkNotNull(savedStateHandle.get<ProductDraftModel>("draft")).copy(
                                originalPrice = originalPrice,
                                salePrice = salePrice,
                                quantity = state.quantity,
                            ),
                        ),
                    )
                }
            }

            ProductPriceAction.NavigationBackClicked -> _event.trySend(ProductPriceEvent.Back)
        }
    }

    private fun update(state: ProductPriceState) {
        savedStateHandle[STATE] = state
    }

    private companion object {
        const val STATE = "priceState"
    }
}
