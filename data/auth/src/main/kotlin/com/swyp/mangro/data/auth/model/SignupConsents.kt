package com.swyp.mangro.data.auth.model

data class SignupConsents(
    val service: Boolean,
    val privacy: Boolean,
    val location: Boolean,
    val thirdParty: Boolean,
    val marketing: Boolean,
) : java.io.Serializable
