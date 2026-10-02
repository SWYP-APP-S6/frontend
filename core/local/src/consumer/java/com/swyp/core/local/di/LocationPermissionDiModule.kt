package com.swyp.core.local.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.swyp.core.local.impl.LocationPermissionStoreImpl
import com.swyp.core.local.store.LocationPermissionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationPermissionDiModule {
    @Provides
    @Singleton
    fun provideLocationPermissionStore(
        @ApplicationContext context: Context,
    ): LocationPermissionStore = LocationPermissionStoreImpl(
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(context.noBackupFilesDir, "location_permission.preferences_pb") },
        ),
    )
}
