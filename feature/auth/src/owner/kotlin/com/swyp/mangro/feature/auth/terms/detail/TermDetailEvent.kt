package com.swyp.mangro.feature.auth.terms.detail

sealed interface TermDetailEvent {
    data object NavigateBack : TermDetailEvent
    data object RefreshTerms : TermDetailEvent
}
