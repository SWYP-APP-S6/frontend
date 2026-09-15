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
)

data class TermsUiState(
    val items: List<TermsItem> = TermsType.entries.map { it.toUiModel() },
) {
    val isAllChecked: Boolean get() = items.all { it.isChecked }
    val isRequiredAllChecked: Boolean
        get() = items.filter { it.type.isRequired }.all { it.isChecked }
}

private fun TermsType.toUiModel() = TermsItem(type = this)
