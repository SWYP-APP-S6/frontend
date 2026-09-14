package com.swyp.mangro.data.owner.auth.storage

internal class OwnerTokens(val accessToken: String, val refreshToken: String)

internal interface OwnerTokenStore {
    fun read(): OwnerTokens?
    fun write(tokens: OwnerTokens)
    fun clear()
}
