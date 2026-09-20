package com.swyp.core.local.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.swyp.core.local.store.InstallIdStore
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal class InstallIdStoreImpl(
    private val dataStore: DataStore<Preferences>,
) : InstallIdStore {
    override suspend fun getOrCreate(): String = withContext(Dispatchers.IO) {
        val existing = dataStore.data.first()[INSTALL_ID_KEY]
        if (existing != null) return@withContext existing

        val newId = UUID.randomUUID().toString()
        dataStore.edit { it[INSTALL_ID_KEY] = newId }
        newId
    }

    private companion object {
        val INSTALL_ID_KEY = stringPreferencesKey("installId")
    }
}
