package com.swyp.mangro.feature.auth.terms.detail

import com.swyp.mangro.data.owner.terms.model.OwnerTermDetail
import com.swyp.mangro.data.owner.terms.model.TermsFailure

data class TermDetailState(
    val detail: OwnerTermDetail? = null,
    val isLoading: Boolean = true,
    val failure: TermsFailure? = null,
)
