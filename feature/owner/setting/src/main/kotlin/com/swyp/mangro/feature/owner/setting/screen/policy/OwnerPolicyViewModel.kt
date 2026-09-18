package com.swyp.mangro.feature.owner.setting.screen.policy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.TermsKind
import com.swyp.mangro.data.auth.repository.TermsRepository
import com.swyp.mangro.feature.owner.setting.model.OwnerPolicy
import com.swyp.mangro.feature.owner.setting.navigation.OwnerPolicyDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OwnerPolicyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TermsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        OwnerPolicyUiState(policy = savedStateHandle.toRoute<OwnerPolicyDestination>().policy),
    )
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<OwnerPolicyEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        load()
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, hasError = false) }
        viewModelScope.launch {
            val kind = when (uiState.value.policy) {
                OwnerPolicy.TERMS_OF_SERVICE -> TermsKind.SERVICE
                OwnerPolicy.PRIVACY_POLICY -> TermsKind.PRIVACY_POLICY
            }
            val documents = repository.fetchTermsDocuments().first()
            val document = (documents as? AuthResult.Success)?.value?.singleOrNull { it.type == kind }
            val result = document?.let { repository.fetchTermsDocument(it.id).first() }
            val detail = (result as? AuthResult.Success)?.value
            if (detail != null && detail.type == kind && detail.content.isNotBlank()) {
                _uiState.update { it.copy(content = detail.content, isLoading = false, hasError = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, hasError = true) }
            }
        }
    }

    fun handleAction(action: OwnerPolicyAction) {
        when (action) {
            OwnerPolicyAction.RetryClicked -> load()
            OwnerPolicyAction.NavigationBackClicked -> _event.trySend(OwnerPolicyEvent.NavigateBack)
        }
    }
}
