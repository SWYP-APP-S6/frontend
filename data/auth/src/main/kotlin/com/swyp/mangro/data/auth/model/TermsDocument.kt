package com.swyp.mangro.data.auth.model

enum class TermsKind { SERVICE, PRIVACY_COLLECTION, LOCATION, THIRD_PARTY, MARKETING, PRIVACY_POLICY }

data class TermsDocument(
    val id: Long,
    val type: TermsKind,
    val title: String,
    val version: Int,
    val required: Boolean,
    val content: String = "",
)
