package com.swyp.mangro.notification.di

import com.swyp.mangro.notification.provider.OwnerNotificationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object OwnerNotificationProviderModule {
    @Provides
    @Singleton
    fun provideOwnerNotificationProvider(): OwnerNotificationProvider = OwnerNotificationProvider()
}
