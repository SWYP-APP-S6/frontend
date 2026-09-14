package com.swyp.mangro.data.owner.auth.di

import com.swyp.mangro.core.network.NetworkClient
import com.swyp.mangro.data.owner.auth.OwnerAuthRepository
import com.swyp.mangro.data.owner.auth.OwnerSessionInterceptor
import com.swyp.mangro.data.owner.auth.RemoteOwnerAuthRepository
import com.swyp.mangro.data.owner.auth.storage.EncryptedOwnerTokenStore
import com.swyp.mangro.data.owner.terms.repository.OwnerTermsRepository
import com.swyp.mangro.remote.auth.service.AuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
internal object OwnerAuthModule {
    @Provides @Singleton
    fun repository(store: EncryptedOwnerTokenStore, terms: OwnerTermsRepository): RemoteOwnerAuthRepository = RemoteOwnerAuthRepository(NetworkClient.create().create(AuthService::class.java), store, terms)

    @Provides @Singleton @OwnerAuthenticated
    fun authenticatedClient(repository: RemoteOwnerAuthRepository): OkHttpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .addInterceptor(OwnerSessionInterceptor(repository))
        .build()

    @Provides fun ownerAuthRepository(repository: RemoteOwnerAuthRepository): OwnerAuthRepository = repository
}
