package com.swyp.core.crypto.di

import com.swyp.core.crypto.impl.AndroidKeyStoreCryptoManagerImpl
import com.swyp.core.crypto.manager.CryptoManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CryptoDiModule {
    @Binds
    @Singleton
    abstract fun bindCryptoManager(impl: AndroidKeyStoreCryptoManagerImpl): CryptoManager
}
