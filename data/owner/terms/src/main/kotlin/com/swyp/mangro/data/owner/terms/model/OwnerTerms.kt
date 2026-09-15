package com.swyp.mangro.data.owner.terms.model

enum class TermsRequirement { REQUIRED, OPTIONAL, NOTICE }

data class OwnerTerm(
    val id: Long,
    val type: String,
    val version: Int,
    val title: String,
    val requirement: TermsRequirement,
    val effectiveDate: String?,
) {
    val isCheckable: Boolean get() = requirement != TermsRequirement.NOTICE
    val selectionKey: String get() = "$id:$version"
}

data class OwnerTermDetail(val document: OwnerTerm, val contentMarkdown: String)

sealed interface TermsResult<out T> {
    data class Success<T>(val value: T) : TermsResult<T>
    data class Failure(val reason: TermsFailure) : TermsResult<Nothing>
}

enum class TermsFailure { NETWORK, NOT_FOUND, INVALID_RESPONSE, SERVER }
