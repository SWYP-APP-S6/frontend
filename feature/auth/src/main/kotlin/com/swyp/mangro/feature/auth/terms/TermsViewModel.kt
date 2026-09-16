package com.swyp.mangro.feature.auth.terms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.mangro.data.auth.BuildConfig
import com.swyp.mangro.data.auth.model.AuthFailure
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.SignupConsents
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.auth.repository.TermsRepository
import com.swyp.mangro.feature.auth.util.consentItems
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TermsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val termsRepository: TermsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TermsUiState())
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<TermsUiEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadTerms()
    }

    fun handleAction(action: TermsUiAction) {
        if (_uiState.value.isLoading) return
        when (action) {
            TermsUiAction.RetryClicked -> loadTerms()

            TermsUiAction.AllAgreeClicked -> {
                if (!_uiState.value.documentsLoaded) return
                val checked = !_uiState.value.isAllChecked
                _uiState.update { it.copy(items = it.items.map { item -> item.copy(isChecked = checked) }) }
            }

            is TermsUiAction.ItemToggled -> {
                if (!_uiState.value.documentsLoaded) return
                _uiState.update { state -> state.copy(items = state.items.map { if (it.type == action.type) it.copy(isChecked = !it.isChecked) else it }) }
            }

            is TermsUiAction.ItemDetailClicked -> viewModelScope.launch { _event.send(TermsUiEvent.NavigateToTermsDetail(action.type)) }

            TermsUiAction.ConfirmClicked -> signup()
        }
    }

    private fun loadTerms() {
        _uiState.update { it.copy(isLoading = true, loadFailed = false) }
        viewModelScope.launch {
            try {
                val items = fetchItems()
                if (items != null) _uiState.update { it.copy(items = items, documentsLoaded = true) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun fetchItems(): List<TermsItem>? {
        val result = termsRepository.fetchTermsDocuments().single()
        val items = (result as? AuthResult.Success)?.value?.let { runCatching { it.consentItems() }.getOrNull() }
        if (items == null) {
            _uiState.update { it.copy(documentsLoaded = false, loadFailed = true) }
            _event.send(TermsUiEvent.LoadFailed)
        }
        return items
    }

    private fun signup() {
        if (!_uiState.value.isRequiredAllChecked) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val latest = fetchItems() ?: return@launch
                if (latest != _uiState.value.items.map { it.copy(isChecked = false) }) {
                    _uiState.update { it.copy(items = latest, documentsLoaded = true) }
                    _event.send(TermsUiEvent.TermsChanged)
                    return@launch
                }

                fun agreed(type: TermsType) = _uiState.value.items.any { it.type == type && it.isChecked }

                val consents = SignupConsents(
                    agreed(TermsType.SERVICE),
                    agreed(TermsType.PRIVACY),
                    agreed(TermsType.LOCATION),
                    agreed(TermsType.PRIVACY_THIRD_PARTY),
                    agreed(TermsType.MARKETING),
                )
                if (BuildConfig.IS_OWNER) {
                    _event.send(TermsUiEvent.OwnerOnboardingRequired(consents))
                    return@launch
                }
                authRepository.signup(consents).collect { result ->
                    when (result) {
                        is AuthResult.Success -> _event.send(TermsUiEvent.SignupCompleted)
                        is AuthResult.Failure -> _event.send(TermsUiEvent.ShowSignupFailure(result.reason == AuthFailure.SIGNUP_REQUIRED))
                    }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
