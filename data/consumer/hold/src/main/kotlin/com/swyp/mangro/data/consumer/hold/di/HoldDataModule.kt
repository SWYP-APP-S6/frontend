package com.swyp.mangro.data.consumer.hold.di

import com.swyp.mangro.data.consumer.hold.impl.HoldRepositoryImpl
import com.swyp.mangro.data.consumer.hold.repository.HoldRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class HoldDataModule {
    @Binds
    @Singleton
    abstract fun bindRepository(impl: HoldRepositoryImpl): HoldRepository
}
