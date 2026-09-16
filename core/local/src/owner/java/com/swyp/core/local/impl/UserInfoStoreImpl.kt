package com.swyp.core.local.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import com.swyp.core.crypto.manager.CryptoManager
import com.swyp.core.local.model.BusinessDay
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
        if (
            preferences[ID] == null &&
            preferences[NAME] == null &&
            preferences[PHONE] == null &&
            preferences[POSTAL_CODE] == null &&
            preferences[ADDRESS] == null &&
            preferences[ADDRESS_DETAIL] == null &&
            preferences[BUSINESS_OPEN_TIME] == null &&
            preferences[BUSINESS_CLOSE_TIME] == null &&
            preferences[BUSINESS_DAYS] == null
        ) {
            null
        } else {
            UserInfo(
                id = decrypt(checkNotNull(preferences[ID]) { "Stored user info is incomplete: id" }).toLong(),
                name = decrypt(checkNotNull(preferences[NAME]) { "Stored user info is incomplete: name" }),
                phone = decrypt(checkNotNull(preferences[PHONE]) { "Stored user info is incomplete: phone" }),
                postalCode = preferences[POSTAL_CODE]?.let { decrypt(it) },
                address = decrypt(checkNotNull(preferences[ADDRESS]) { "Stored user info is incomplete: address" }),
                addressDetail = preferences[ADDRESS_DETAIL]?.let { decrypt(it) },
                businessOpenTime = decrypt(checkNotNull(preferences[BUSINESS_OPEN_TIME]) { "Stored user info is incomplete: businessOpenTime" }),
                businessCloseTime = decrypt(checkNotNull(preferences[BUSINESS_CLOSE_TIME]) { "Stored user info is incomplete: businessCloseTime" }),
                businessDays = decrypt(checkNotNull(preferences[BUSINESS_DAYS]) { "Stored user info is incomplete: businessDays" }).let { value ->
                    if (value.isEmpty()) emptyList() else value.split(",").map { BusinessDay.valueOf(it) }
                },
            )
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    override suspend fun save(userInfo: UserInfo) {
        withContext(Dispatchers.IO) {
            val id = encrypt(userInfo.id.toString())
            val name = encrypt(userInfo.name)
            val phone = encrypt(userInfo.phone)
            val postalCode = userInfo.postalCode?.let { encrypt(it) }
            val address = encrypt(userInfo.address)
            val addressDetail = userInfo.addressDetail?.let { encrypt(it) }
            val businessOpenTime = encrypt(userInfo.businessOpenTime)
            val businessCloseTime = encrypt(userInfo.businessCloseTime)
            val businessDays = encrypt(userInfo.businessDays.joinToString(",") { it.name })

            dataStore.edit { preferences ->
                preferences[ID] = id
                preferences[NAME] = name
                preferences[PHONE] = phone
                if (postalCode == null) preferences.remove(POSTAL_CODE) else preferences[POSTAL_CODE] = postalCode
                preferences[ADDRESS] = address
                if (addressDetail == null) preferences.remove(ADDRESS_DETAIL) else preferences[ADDRESS_DETAIL] = addressDetail
                preferences[BUSINESS_OPEN_TIME] = businessOpenTime
                preferences[BUSINESS_CLOSE_TIME] = businessCloseTime
                preferences[BUSINESS_DAYS] = businessDays
            }
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(ID)
            preferences.remove(NAME)
            preferences.remove(PHONE)
            preferences.remove(POSTAL_CODE)
            preferences.remove(ADDRESS)
            preferences.remove(ADDRESS_DETAIL)
            preferences.remove(BUSINESS_OPEN_TIME)
            preferences.remove(BUSINESS_CLOSE_TIME)
            preferences.remove(BUSINESS_DAYS)
        }
    }

    private fun encrypt(value: String): ByteArray = cryptoManager.encrypt(value.encodeToByteArray())

    private fun decrypt(value: ByteArray): String = cryptoManager.decrypt(value).decodeToString(throwOnInvalidSequence = true)

    private companion object {
        val ID = byteArrayPreferencesKey("id")
        val NAME = byteArrayPreferencesKey("name")
        val PHONE = byteArrayPreferencesKey("phone")
        val POSTAL_CODE = byteArrayPreferencesKey("postalCode")
        val ADDRESS = byteArrayPreferencesKey("address")
        val ADDRESS_DETAIL = byteArrayPreferencesKey("addressDetail")
        val BUSINESS_OPEN_TIME = byteArrayPreferencesKey("businessOpenTime")
        val BUSINESS_CLOSE_TIME = byteArrayPreferencesKey("businessCloseTime")
        val BUSINESS_DAYS = byteArrayPreferencesKey("businessDays")
    }
}
