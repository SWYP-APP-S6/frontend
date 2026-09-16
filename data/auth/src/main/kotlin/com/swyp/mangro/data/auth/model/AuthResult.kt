package com.swyp.mangro.data.auth.model

sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Failure(val reason: AuthFailure) : AuthResult<Nothing>
}

enum class AuthFailure {
    NETWORK,
    SERVER,
    INVALID_OAUTH_TOKEN,
    INVALID_RESPONSE,
    STORAGE,
    SIGNUP_REQUIRED,
}
