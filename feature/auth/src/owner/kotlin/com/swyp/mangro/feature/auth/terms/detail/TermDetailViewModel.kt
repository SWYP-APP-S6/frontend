package com.swyp.mangro.feature.auth.terms.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.data.owner.terms.model.TermsResult
import com.swyp.mangro.data.owner.terms.repository.OwnerTermsRepository
import com.swyp.mangro.feature.auth.navigation.OwnerTermDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TermDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: OwnerTermsRepository,
) : ViewModel() {
    private val documentId = savedStateHandle.toRoute<OwnerTermDetailDestination>().documentId
    private val _uiState = MutableStateFlow(TermDetailState())
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<TermDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()
    private var fetchJob: Job? = null

    init {
        fetchTerm()
    }

    fun handleAction(action: TermDetailAction) {
        when (action) {
            TermDetailAction.RetryClicked -> fetchTerm()
            TermDetailAction.BackClicked -> _event.trySend(TermDetailEvent.NavigateBack)
            TermDetailAction.RefreshListClicked -> _event.trySend(TermDetailEvent.RefreshTerms)
        }
    }

    private fun fetchTerm() {
        if (fetchJob?.isActive == true) return
        _uiState.value = TermDetailState()
        fetchJob = viewModelScope.launch {
            _uiState.value = when (val result = repository.fetchTerm(documentId)) {
                is TermsResult.Success -> TermDetailState(detail = result.value, isLoading = false)
                is TermsResult.Failure -> TermDetailState(isLoading = false, failure = result.reason)
            }
        }
    }
}
