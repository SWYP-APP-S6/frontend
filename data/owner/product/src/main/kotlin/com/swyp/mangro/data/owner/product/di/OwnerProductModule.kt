package com.swyp.mangro.data.owner.product.di

import com.swyp.mangro.data.owner.product.impl.OwnerProductRepositoryImpl
import com.swyp.mangro.data.owner.product.repository.OwnerProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class OwnerProductModule {
    @Binds abstract fun bindRepository(impl: OwnerProductRepositoryImpl): OwnerProductRepository
}
