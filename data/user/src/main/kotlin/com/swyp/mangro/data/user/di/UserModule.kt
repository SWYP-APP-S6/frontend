package com.swyp.mangro.data.user.di

import com.swyp.mangro.data.user.impl.UserRepositoryImpl
import com.swyp.mangro.data.user.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UserModule {
    @Binds
    @Singleton
    abstract fun bindRepository(implementation: UserRepositoryImpl): UserRepository
}
