package com.swyp.mangro.feature.auth.terms

import com.swyp.mangro.data.owner.auth.AuthFailure
import com.swyp.mangro.data.owner.terms.model.OwnerTerm
import com.swyp.mangro.data.owner.terms.model.TermsFailure
import com.swyp.mangro.data.owner.terms.model.TermsRequirement

data class TermsItem(val document: OwnerTerm, val isChecked: Boolean = false)

data class TermsUiState(
    val items: List<TermsItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val signupFailure: AuthFailure? = null,
    val failure: TermsFailure? = null,
) {
    val isAllChecked: Boolean get() = items.any { it.document.isCheckable } && items.filter { it.document.isCheckable }.all { it.isChecked }
    val isRequiredAllChecked: Boolean get() = !isLoading && !isSubmitting && failure == null && items.isNotEmpty() && items.filter { it.document.requirement == TermsRequirement.REQUIRED }.all { it.isChecked }
}
