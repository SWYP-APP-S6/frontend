package com.swyp.mangro.di

import com.swyp.mangro.feature.owner.onboarding.util.StoreRegistrationSubmitter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object OwnerRegistrationModule {
    @Provides
    fun provideStoreRegistrationSubmitter(): StoreRegistrationSubmitter = StoreRegistrationSubmitter { error("Store registration API is not configured") }
}
