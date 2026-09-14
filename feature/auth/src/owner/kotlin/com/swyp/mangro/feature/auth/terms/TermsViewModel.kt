package com.swyp.mangro.feature.auth.terms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.owner.auth.AuthFailure
import com.swyp.mangro.data.owner.auth.AuthResult
import com.swyp.mangro.data.owner.auth.OwnerAuthRepository
import com.swyp.mangro.data.owner.terms.model.TermsResult
import com.swyp.mangro.data.owner.terms.repository.OwnerTermsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TermsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: OwnerTermsRepository,
    private val auth: OwnerAuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TermsUiState())
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<TermsUiEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()
    private var fetchJob: Job? = null

    init {
        fetchTerms()
    }

    fun handleAction(action: TermsUiAction) {
        val state = _uiState.value
        if (state.isSubmitting) return
        when (action) {
            TermsUiAction.RetryClicked -> fetchTerms()
            TermsUiAction.AllAgreeClicked -> if (!state.isLoading && state.failure == null) {
                updateItems(state.items.map { it.copy(isChecked = it.document.isCheckable && !state.isAllChecked) })
            }
            is TermsUiAction.ItemToggled -> if (!state.isLoading && state.failure == null) {
                updateItems(state.items.map { if (it.document.id == action.id && it.document.isCheckable) it.copy(isChecked = !it.isChecked) else it })
            }
            is TermsUiAction.ItemDetailClicked -> if (state.items.any { it.document.id == action.id }) {
                _event.trySend(TermsUiEvent.NavigateToTermsDetail(action.id))
            }
            TermsUiAction.ConfirmClicked -> if (state.isRequiredAllChecked) submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        _uiState.value = state.copy(isSubmitting = true, signupFailure = null)
        viewModelScope.launch {
            when (val result = auth.signup(state.items.map { it.document }, state.items.filter { it.isChecked }.map { it.document.selectionKey }.toSet())) {
                is AuthResult.Success -> {
                    _event.send(TermsUiEvent.NavigateToOnboarding)
                }
                is AuthResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, signupFailure = result.reason)
                    when (result.reason) {
                        AuthFailure.UNAUTHORIZED -> _event.send(TermsUiEvent.NavigateToLogin)
                        AuthFailure.CONSENT_CHANGED -> fetchTerms()
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun fetchTerms() {
        if (fetchJob?.isActive == true) return
        _uiState.value = _uiState.value.copy(isLoading = true, failure = null)
        fetchJob = viewModelScope.launch {
            when (val result = repository.fetchTerms()) {
                is TermsResult.Success -> {
                    val selected = savedStateHandle.get<ArrayList<String>>("selectedTerms").orEmpty().toSet()
                    _uiState.value = TermsUiState(items = result.value.map { TermsItem(it, it.isCheckable && it.selectionKey in selected) }, isLoading = false, signupFailure = _uiState.value.signupFailure)
                    persistSelection()
                }
                is TermsResult.Failure -> _uiState.value = _uiState.value.copy(isLoading = false, failure = result.reason)
            }
        }
    }

    private fun updateItems(items: List<TermsItem>) {
        _uiState.value = _uiState.value.copy(items = items)
        persistSelection()
    }

    private fun persistSelection() {
        savedStateHandle["selectedTerms"] = ArrayList(_uiState.value.items.filter { it.isChecked }.map { it.document.selectionKey })
    }
}
