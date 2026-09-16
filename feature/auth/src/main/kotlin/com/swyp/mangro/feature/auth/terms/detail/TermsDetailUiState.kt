package com.swyp.mangro.feature.auth.terms.detail

data class TermsDetailUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val content: String = "",
    val failed: Boolean = false,
)
