package com.swyp.mangro.data.owner.terms.di

import com.swyp.mangro.data.owner.terms.repository.OwnerTermsRepository
import com.swyp.mangro.data.owner.terms.repository.RemoteOwnerTermsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface OwnerTermsModule {
    @Binds
    @Singleton
    fun bindOwnerTermsRepository(implementation: RemoteOwnerTermsRepository): OwnerTermsRepository
}
