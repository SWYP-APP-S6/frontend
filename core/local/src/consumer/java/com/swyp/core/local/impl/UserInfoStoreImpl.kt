package com.swyp.core.local.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import com.swyp.core.crypto.manager.CryptoManager
import com.swyp.core.local.model.UserInfo
import com.swyp.core.local.store.UserInfoStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class UserInfoStoreImpl(
    private val dataStore: DataStore<Preferences>,
    private val cryptoManager: CryptoManager,
) : UserInfoStore {
    override val userInfo: Flow<UserInfo?> = dataStore.data.map { preferences ->
        if (preferences[USER_ID] == null && preferences[NICKNAME] == null && preferences[PHONE] == null) {
            null
        } else {
            UserInfo(
                userId = decrypt(checkNotNull(preferences[USER_ID]) { "Stored user info is incomplete: userId" }).toLong(),
                nickname = decrypt(checkNotNull(preferences[NICKNAME]) { "Stored user info is incomplete: nickname" }),
                phone = preferences[PHONE]?.let { decrypt(it) },
            )
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    override suspend fun save(userInfo: UserInfo) {
        withContext(Dispatchers.IO) {
            val userId = encrypt(userInfo.userId.toString())
            val nickname = encrypt(userInfo.nickname)
            val phone = userInfo.phone?.let { encrypt(it) }

            dataStore.edit { preferences ->
                preferences[USER_ID] = userId
                preferences[NICKNAME] = nickname
                if (phone == null) preferences.remove(PHONE) else preferences[PHONE] = phone
            }
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(NICKNAME)
            preferences.remove(PHONE)
        }
    }

    private fun encrypt(value: String): ByteArray = cryptoManager.encrypt(value.encodeToByteArray())

    private fun decrypt(value: ByteArray): String = cryptoManager.decrypt(value).decodeToString(throwOnInvalidSequence = true)

    private companion object {
        val USER_ID = byteArrayPreferencesKey("userId")
        val NICKNAME = byteArrayPreferencesKey("nickname")
        val PHONE = byteArrayPreferencesKey("phone")
    }
}
