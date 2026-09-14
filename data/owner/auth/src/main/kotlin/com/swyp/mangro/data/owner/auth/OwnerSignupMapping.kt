package com.swyp.mangro.data.owner.auth

import com.swyp.mangro.data.owner.terms.model.OwnerTerm
import com.swyp.mangro.data.owner.terms.model.TermsRequirement
import com.swyp.mangro.remote.auth.model.RegisterUserRequest

internal fun signupRequest(token: String, documents: List<OwnerTerm>, selected: Set<String>): RegisterUserRequest {
    val supported = setOf("SERVICE", "PRIVACY_COLLECTION", "MARKETING", "LOCATION", "THIRD_PARTY")
    if (documents.isEmpty() || documents.filter { it.requirement == TermsRequirement.REQUIRED }.any { it.selectionKey !in selected }) {
        throw AuthException(AuthFailure.CONSENT_CHANGED)
    }
    val checked = documents.filter { it.isCheckable && it.selectionKey in selected }.map { it.type }.toSet()
    if (!supported.containsAll(checked)) throw AuthException(AuthFailure.CONSENT_CHANGED)
    return RegisterUserRequest(
        signupToken = token,
        serviceTermsAgreed = "SERVICE" in checked,
        privacyTermsAgreed = "PRIVACY_COLLECTION" in checked,
        marketingOptIn = "MARKETING" in checked,
        locationTermsAgreed = "LOCATION" in checked,
        thirdPartyTermsAgreed = "THIRD_PARTY" in checked,
    )
}
