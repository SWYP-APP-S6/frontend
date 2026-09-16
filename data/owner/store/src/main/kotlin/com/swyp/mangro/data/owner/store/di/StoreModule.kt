package com.swyp.mangro.data.owner.store.di

import com.swyp.mangro.data.owner.store.impl.StoreRepositoryImpl
import com.swyp.mangro.data.owner.store.repository.StoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class StoreModule {
    @Binds
    @Singleton
    abstract fun bindStoreRepository(implementation: StoreRepositoryImpl): StoreRepository
}
