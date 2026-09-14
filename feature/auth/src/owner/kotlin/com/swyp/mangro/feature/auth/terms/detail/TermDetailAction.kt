package com.swyp.mangro.feature.auth.terms.detail

sealed interface TermDetailAction {
    data object RetryClicked : TermDetailAction
    data object BackClicked : TermDetailAction
    data object RefreshListClicked : TermDetailAction
}
