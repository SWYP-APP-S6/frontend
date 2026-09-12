package com.swyp.mangro.feature.owner.onboarding.screen.operating

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.feature.owner.onboarding.Constants.BASIC_INFO
import com.swyp.mangro.feature.owner.onboarding.model.StoreBasicInfoModel
import com.swyp.mangro.feature.owner.onboarding.model.StoreRegistrationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OwnerOperatingInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val basicInfo = requireNotNull(savedStateHandle.get<StoreBasicInfoModel>(BASIC_INFO))
    private val restored = savedStateHandle.get<StoreRegistrationModel>("registration")
    private val _uiState = MutableStateFlow(
        OwnerOperatingInfoState(
            basicInfo = basicInfo,
            phoneNumber = savedStateHandle["phoneNumber"] ?: "",
            openingMinutes = restored?.openingMinutes,
            closingMinutes = restored?.closingMinutes,
            businessDays = restored?.businessDays.orEmpty().toPersistentSet(),
            dialog = savedStateHandle.get<String>("dialog")?.let { value -> OwnerOperatingInfoDialog.entries.firstOrNull { it.name == value } },
            isCompletionHandled = savedStateHandle["completionHandled"] ?: false,
        ),
    )
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<OwnerOperatingInfoEvent>(Channel.BUFFERED)
    val event: Flow<OwnerOperatingInfoEvent> = _event.receiveAsFlow()

    fun handleAction(action: OwnerOperatingInfoAction) {
        val state = _uiState.value

        if (state.isLoading) return
        if (state.dialog == OwnerOperatingInfoDialog.Submitted && action != OwnerOperatingInfoAction.CompletionConfirmed) return

        when (action) {
            is OwnerOperatingInfoAction.PhoneNumberChanged -> if (action.value.length <= 13 && action.value.all { it.isDigit() || it == '-' || it == ' ' }) updateState(state.copy(phoneNumber = action.value))

            is OwnerOperatingInfoAction.OpeningTimeSelected -> if (action.minutes in 0..1380 && action.minutes % 60 == 0) {
                updateState(state.copy(openingMinutes = action.minutes, closingMinutes = state.closingMinutes?.takeIf { it >= action.minutes }))
            }

            is OwnerOperatingInfoAction.ClosingTimeSelected -> if (state.openingMinutes != null && action.minutes in state.openingMinutes..1380 && action.minutes % 60 == 0) {
                updateState(state.copy(closingMinutes = action.minutes))
            }

            is OwnerOperatingInfoAction.BusinessDayClicked -> if (action.day in 0..6) {
                updateState(
                    state.copy(
                        businessDays = if (action.day in state.businessDays) {
                            state.businessDays.remove(action.day)
                        } else {
                            state.businessDays.add(action.day)
                        },
                    ),
                )
            }

            OwnerOperatingInfoAction.DialogDismissed -> updateState(state.copy(dialog = null))

            OwnerOperatingInfoAction.SubmitClicked -> submit()

            OwnerOperatingInfoAction.NavigationBackClicked -> sendEvent(OwnerOperatingInfoEvent.NavigateBack)

            OwnerOperatingInfoAction.CompletionConfirmed -> if (state.dialog == OwnerOperatingInfoDialog.Submitted && !state.isCompletionHandled) {
                updateState(state.copy(isCompletionHandled = true))
                sendEvent(OwnerOperatingInfoEvent.CompleteOnboarding)
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled) return
        // TODO: 매장 등록 Repository 연결 후 실제 응답에 따라 상태를 갱신한다.
        updateState(state.copy(dialog = OwnerOperatingInfoDialog.Error))
    }

    private fun updateState(state: OwnerOperatingInfoState) {
        _uiState.update { state }

        savedStateHandle["registration"] = state.registration
        savedStateHandle["phoneNumber"] = state.phoneNumber
        savedStateHandle["dialog"] = state.dialog?.name
        savedStateHandle["completionHandled"] = state.isCompletionHandled
    }

    private fun sendEvent(event: OwnerOperatingInfoEvent) {
        viewModelScope.launch { _event.send(event) }
    }
}
