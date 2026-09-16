package com.swyp.mangro.data.owner.home.di

import com.swyp.mangro.data.owner.home.impl.OwnerHomeRepositoryImpl
import com.swyp.mangro.data.owner.home.repository.OwnerHomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class OwnerHomeModule {
    @Binds
    @Singleton
    abstract fun bindRepository(implementation: OwnerHomeRepositoryImpl): OwnerHomeRepository
}
