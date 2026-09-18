package com.swyp.mangro.data.consumer.product.di

import com.swyp.mangro.data.consumer.product.impl.ProductDetailRepositoryImpl
import com.swyp.mangro.data.consumer.product.repository.ProductDetailRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProductDetailDataModule {
    @Binds
    @Singleton
    abstract fun bindRepository(impl: ProductDetailRepositoryImpl): ProductDetailRepository
}
