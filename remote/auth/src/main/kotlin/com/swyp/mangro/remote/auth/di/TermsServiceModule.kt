package com.swyp.mangro.remote.auth.di

import com.swyp.mangro.core.network.NetworkClient
import com.swyp.mangro.remote.auth.service.TermsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Public documents must remain available without a session or token refresh. */
@Module
@InstallIn(SingletonComponent::class)
object TermsServiceModule {
    @Provides
    @Singleton
    fun provideTermsService(): TermsService = NetworkClient.create().create(TermsService::class.java)
}
