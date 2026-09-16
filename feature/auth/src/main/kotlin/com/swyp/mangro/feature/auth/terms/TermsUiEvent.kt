package com.swyp.mangro.feature.auth.terms

import com.swyp.mangro.data.auth.model.SignupConsents

sealed interface TermsUiEvent {
    data object TermsChanged : TermsUiEvent
    data object LoadFailed : TermsUiEvent
    data class ShowSignupFailure(val needsLogin: Boolean) : TermsUiEvent
    data object SignupCompleted : TermsUiEvent
    data class OwnerOnboardingRequired(val consents: SignupConsents) : TermsUiEvent
    data class NavigateToTermsDetail(val type: TermsType) : TermsUiEvent
}
