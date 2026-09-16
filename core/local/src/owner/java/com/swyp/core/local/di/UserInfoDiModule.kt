package com.swyp.core.local.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.swyp.core.crypto.manager.CryptoManager
import com.swyp.core.local.impl.UserInfoStoreImpl
import com.swyp.core.local.store.UserInfoStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserInfoDiModule {
    @Provides
    @Singleton
    fun provideUserInfoStore(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager,
    ): UserInfoStore = UserInfoStoreImpl(
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(context.noBackupFilesDir, "user_info.preferences_pb") },
        ),
        cryptoManager = cryptoManager,
    )
}
