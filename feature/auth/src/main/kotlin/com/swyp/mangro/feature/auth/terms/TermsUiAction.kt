package com.swyp.mangro.feature.auth.terms

sealed interface TermsUiAction {
    data object RetryClicked : TermsUiAction
    data object AllAgreeClicked : TermsUiAction
    data class ItemToggled(val type: TermsType) : TermsUiAction
    data class ItemDetailClicked(val type: TermsType) : TermsUiAction
    data object ConfirmClicked : TermsUiAction
}
