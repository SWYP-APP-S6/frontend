package com.swyp.mangro.feature.auth.terms

sealed interface TermsUiEvent {
    data object NavigateToHome : TermsUiEvent
    data class NavigateToTermsDetail(val type: TermsType) : TermsUiEvent
}
