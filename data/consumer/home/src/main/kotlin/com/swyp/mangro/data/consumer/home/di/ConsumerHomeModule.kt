package com.swyp.mangro.data.consumer.home.di

import com.swyp.mangro.data.consumer.home.impl.ConsumerHomeRepositoryImpl
import com.swyp.mangro.data.consumer.home.repository.ConsumerHomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ConsumerHomeModule {
    @Binds
    @Singleton
    abstract fun bindRepository(implementation: ConsumerHomeRepositoryImpl): ConsumerHomeRepository
}
