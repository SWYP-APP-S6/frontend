package com.swyp.mangro.feature.auth.terms

sealed interface TermsUiEvent {
    data object NavigateToLogin : TermsUiEvent
    data object NavigateToOnboarding : TermsUiEvent
    data class NavigateToTermsDetail(val id: Long) : TermsUiEvent
}
