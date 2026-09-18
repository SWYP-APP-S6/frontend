package com.swyp.mangro.feature.auth.terms.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.swyp.mangro.data.auth.model.AuthResult
import com.swyp.mangro.data.auth.model.TermsKind
import com.swyp.mangro.data.auth.repository.TermsRepository
import com.swyp.mangro.feature.auth.navigation.TermsDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TermsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TermsRepository,
) : ViewModel() {
    private val kind = TermsKind.valueOf(savedStateHandle.toRoute<TermsDetail>().kind)
    private val _uiState = MutableStateFlow(TermsDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchTerm()
    }

    fun fetchTerm() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, failed = false) }

        viewModelScope.launch {
            val documents = repository.fetchTermsDocuments().single()
            val summary = (documents as? AuthResult.Success)?.value?.singleOrNull { it.type == kind }
            val result = summary?.let {
                repository.fetchTermsDocument(it.id).single()
            }
            val document = (result as? AuthResult.Success)?.value

            _uiState.update {
                if (document != null && document.type == kind) {
                    it.copy(
                        isLoading = false,
                        failed = false,
                        title = document.title,
                        content = document.content,
                    )
                } else {
                    it.copy(isLoading = false, failed = true)
                }
            }
        }
    }
}
