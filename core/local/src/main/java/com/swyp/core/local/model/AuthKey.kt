package com.swyp.core.local.model

data class AuthKey(
    val accessToken: String,
    val refreshToken: String,
) {
    init {
        require(accessToken.isNotBlank()) { "Access token must not be blank" }
        require(refreshToken.isNotBlank()) { "Refresh token must not be blank" }
    }

    override fun toString(): String = "AuthKey(accessToken=REDACTED, refreshToken=REDACTED)"
}
