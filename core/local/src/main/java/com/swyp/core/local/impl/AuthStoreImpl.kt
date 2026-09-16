package com.swyp.core.local.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import com.swyp.core.crypto.manager.CryptoManager
import com.swyp.core.local.model.AuthKey
import com.swyp.core.local.store.AuthStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class AuthStoreImpl(
    private val dataStore: DataStore<Preferences>,
    private val cryptoManager: CryptoManager,
) : AuthStore {
    override val authKey: Flow<AuthKey?> = dataStore.data.map { preferences ->
        val accessToken = preferences[ACCESS_TOKEN]
        val refreshToken = preferences[REFRESH_TOKEN]

        if (accessToken == null && refreshToken == null) {
            null
        } else {
            check(accessToken != null && refreshToken != null) { "Stored auth token pair is incomplete" }
            AuthKey(
                accessToken = cryptoManager.decrypt(accessToken).decodeToString(throwOnInvalidSequence = true),
                refreshToken = cryptoManager.decrypt(refreshToken).decodeToString(throwOnInvalidSequence = true),
            )
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    override suspend fun save(authKey: AuthKey) {
        withContext(Dispatchers.IO) {
            val accessToken = cryptoManager.encrypt(authKey.accessToken.encodeToByteArray())
            val refreshToken = cryptoManager.encrypt(authKey.refreshToken.encodeToByteArray())

            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN] = accessToken
                preferences[REFRESH_TOKEN] = refreshToken
            }
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
        }
    }

    private companion object {
        val ACCESS_TOKEN = byteArrayPreferencesKey("accessToken")
        val REFRESH_TOKEN = byteArrayPreferencesKey("refreshToken")
    }
}
