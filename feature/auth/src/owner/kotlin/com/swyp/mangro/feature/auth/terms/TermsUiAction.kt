package com.swyp.mangro.feature.auth.terms

sealed interface TermsUiAction {
    data object AllAgreeClicked : TermsUiAction
    data class ItemToggled(val id: Long) : TermsUiAction
    data class ItemDetailClicked(val id: Long) : TermsUiAction
    data object ConfirmClicked : TermsUiAction
    data object RetryClicked : TermsUiAction
}
