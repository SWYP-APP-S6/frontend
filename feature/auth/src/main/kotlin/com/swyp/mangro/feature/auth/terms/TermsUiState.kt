package com.swyp.mangro.feature.auth.terms

import kotlin.collections.all
import kotlin.collections.filter

enum class TermsType(val isRequired: Boolean) {
    SERVICE(isRequired = true),
    PRIVACY(isRequired = true),
    LOCATION(isRequired = true),
    PRIVACY_THIRD_PARTY(isRequired = true),
    MARKETING(isRequired = false),
}

data class TermsItem(
    val type: TermsType,
    val isChecked: Boolean = false,
    val required: Boolean = type.isRequired,
    val documentId: Long = 0,
    val version: Int = 0,
    val title: String = "",

)

data class TermsUiState(
    val isLoading: Boolean = false,
    val documentsLoaded: Boolean = false,
    val loadFailed: Boolean = false,
    val items: List<TermsItem> = emptyList(),
) {
    val isAllChecked: Boolean get() = items.isNotEmpty() && items.all { it.isChecked }
    val isRequiredAllChecked: Boolean
        get() = documentsLoaded && items.isNotEmpty() && items.filter { it.required }.all { it.isChecked }
}
