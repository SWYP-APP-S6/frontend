package com.swyp.core.local.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.swyp.core.crypto.manager.CryptoManager
import com.swyp.core.local.impl.AuthStoreImpl
import com.swyp.core.local.store.AuthStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiModule {
    @Provides
    @Singleton
    fun provideAuthStore(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager,
    ): AuthStore = AuthStoreImpl(
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(context.noBackupFilesDir, "auth.preferences_pb") },
        ),
        cryptoManager = cryptoManager,
    )
}
