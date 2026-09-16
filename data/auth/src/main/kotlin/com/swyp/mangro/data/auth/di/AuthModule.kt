package com.swyp.mangro.data.auth.di

import com.swyp.mangro.data.auth.impl.AuthRepositoryImpl
import com.swyp.mangro.data.auth.impl.TermsRepositoryImpl
import com.swyp.mangro.data.auth.repository.AuthRepository
import com.swyp.mangro.data.auth.repository.TermsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindTermsRepository(repository: TermsRepositoryImpl): TermsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(repository: AuthRepositoryImpl): AuthRepository
}
