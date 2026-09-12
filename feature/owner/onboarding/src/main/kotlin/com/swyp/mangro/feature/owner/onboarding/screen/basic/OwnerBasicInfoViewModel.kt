package com.swyp.mangro.feature.owner.onboarding.screen.basic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.feature.owner.onboarding.model.StoreBasicInfoModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OwnerBasicInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val restored = savedStateHandle.get<StoreBasicInfoModel>("basicInfo") ?: StoreBasicInfoModel()

    private val _uiState = MutableStateFlow(OwnerBasicInfoState(restored.name, restored.detailedAddress, restored.category, restored.address))
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<OwnerBasicInfoEvent>(Channel.BUFFERED)
    val event: Flow<OwnerBasicInfoEvent> = _event.receiveAsFlow()

    fun handleAction(action: OwnerBasicInfoAction) {
        val state = _uiState.value
        when (action) {
            is OwnerBasicInfoAction.NameChanged -> updateState(state.copy(name = action.value))

            is OwnerBasicInfoAction.DetailedAddressChanged -> updateState(state.copy(detailedAddress = action.value))

            is OwnerBasicInfoAction.CategorySelected -> if (action.value in state.categories) updateState(state.copy(category = action.value))

            is OwnerBasicInfoAction.AddressSelected -> updateState(state.copy(address = action.value))

            is OwnerBasicInfoAction.CategoriesReceived -> updateState(state.copy(categories = action.values.toPersistentList()))

            OwnerBasicInfoAction.NextClicked -> if (state.isNextEnabled) {
                sendEvent(
                    OwnerBasicInfoEvent.NavigateToOperatingInfo(
                        StoreBasicInfoModel(state.name.trim(), state.category, state.address, state.detailedAddress.trim()),
                    ),
                )
            }

            OwnerBasicInfoAction.AddressSearchClicked -> sendEvent(OwnerBasicInfoEvent.NavigateToAddressSearch)

            OwnerBasicInfoAction.NavigationBackClicked -> sendEvent(OwnerBasicInfoEvent.NavigateBack)
        }
    }

    private fun updateState(state: OwnerBasicInfoState) {
        _uiState.update { state }
        savedStateHandle["basicInfo"] = StoreBasicInfoModel(state.name, state.category, state.address, state.detailedAddress)
    }

    private fun sendEvent(event: OwnerBasicInfoEvent) {
        viewModelScope.launch { _event.send(event) }
    }
}
