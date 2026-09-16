package com.swyp.mangro.feature.auth.util

import com.swyp.mangro.data.auth.model.TermsDocument
import com.swyp.mangro.data.auth.model.TermsKind
import com.swyp.mangro.feature.auth.terms.TermsItem
import com.swyp.mangro.feature.auth.terms.TermsType

internal val TermsType.kind: TermsKind
    get() = when (this) {
        TermsType.SERVICE -> TermsKind.SERVICE
        TermsType.PRIVACY -> TermsKind.PRIVACY_COLLECTION
        TermsType.LOCATION -> TermsKind.LOCATION
        TermsType.PRIVACY_THIRD_PARTY -> TermsKind.THIRD_PARTY
        TermsType.MARKETING -> TermsKind.MARKETING
    }

internal fun List<TermsDocument>.consentItems(): List<TermsItem> = TermsType.entries.mapNotNull { type ->
    val document = singleOrNull { it.type == type.kind } ?: return@mapNotNull null
    TermsItem(type, required = document.required, documentId = document.id, version = document.version, title = document.title)
}.also { require(it.isNotEmpty()) }
