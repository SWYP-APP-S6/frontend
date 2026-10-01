package com.swyp.core.local.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.swyp.core.local.store.LocationPermissionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class LocationPermissionStoreImpl(
    private val dataStore: DataStore<Preferences>,
) : LocationPermissionStore {

    override val isIntroShown: Flow<Boolean> =
        dataStore.data.map { it[INTRO_SHOWN_KEY] ?: false }

    override val isRequested: Flow<Boolean> =
        dataStore.data.map { it[REQUESTED_KEY] ?: false }

    override suspend fun markIntroShown() {
        withContext(Dispatchers.IO) {
            dataStore.edit { it[INTRO_SHOWN_KEY] = true }
        }
    }

    override suspend fun markRequested() {
        withContext(Dispatchers.IO) {
            dataStore.edit { it[REQUESTED_KEY] = true }
        }
    }

    private companion object {
        val INTRO_SHOWN_KEY = booleanPreferencesKey("introShown")
        val REQUESTED_KEY = booleanPreferencesKey("requested")
    }
}
