package com.swyp.mangro.feature.owner.product.screen.editor.basic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.swyp.mangro.feature.owner.product.model.ProductDraftModel
import com.swyp.mangro.feature.owner.product.util.OwnerProductLimits
import com.swyp.mangro.feature.owner.product.util.mergedProductPhotos
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Collections
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class ProductBasicInfoViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val uiState = savedStateHandle.getStateFlow(STATE, ProductBasicInfoState())
    private val _event = Channel<ProductBasicInfoEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun initialize() {
        if (savedStateHandle.get<Boolean>("initialized") == true) return
        savedStateHandle["productId"] = UUID.randomUUID().toString()
        update(ProductBasicInfoState(isLoading = false))
        savedStateHandle["initialized"] = true
    }

    fun handleAction(action: ProductBasicInfoAction) {
        val state = uiState.value
        when (action) {
            is ProductBasicInfoAction.NameChanged -> update(
                state.copy(
                    name = action.name,
                    error = if (action.name.trim().let { it.codePointCount(0, it.length) > OwnerProductLimits.NAME_LENGTH }) {
                        ProductBasicInfoError.INVALID_NAME
                    } else {
                        state.error.takeUnless { it == ProductBasicInfoError.INVALID_NAME }
                    },
                ),
            )
            is ProductBasicInfoAction.PhotosSelected -> update(state.copy(photos = mergedProductPhotos(state.photos, action.photos)))
            is ProductBasicInfoAction.PhotoRemoveClicked -> update(state.copy(photos = state.photos - action.photo))
            ProductBasicInfoAction.PhotoPermissionFailed -> update(state.copy(error = ProductBasicInfoError.PHOTO_PERMISSION))
            ProductBasicInfoAction.ErrorDismissed -> update(state.copy(error = null))
            ProductBasicInfoAction.NextClicked -> if (state.canContinue) _event.trySend(ProductBasicInfoEvent.Next(ProductDraftModel(id = checkNotNull(savedStateHandle["productId"]), name = state.name.trim(), photos = state.photos)))
            ProductBasicInfoAction.NavigationBackClicked -> update(state.copy(showDiscard = true))
            ProductBasicInfoAction.DiscardDismissed -> update(state.copy(showDiscard = false))
            ProductBasicInfoAction.DiscardConfirmClicked -> if (state.showDiscard) {
                update(state.copy(showDiscard = false))
                _event.trySend(ProductBasicInfoEvent.Exit)
            }
        }
    }

    private fun update(state: ProductBasicInfoState) {
        savedStateHandle[STATE] = state.copy(photos = Collections.unmodifiableList(state.photos.toList()))
    }
    private companion object {
        const val STATE = "basicState"
    }
}
