package com.swyp.mangro.data.owner.product.di

import com.swyp.mangro.data.owner.product.impl.ContentProductPhotoSource
import com.swyp.mangro.data.owner.product.impl.ProductPhotoSource
import com.swyp.mangro.data.owner.product.impl.ProductRepositoryImpl
import com.swyp.mangro.data.owner.product.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProductModule {
    @Binds
    @Singleton
    abstract fun bindRepository(implementation: ProductRepositoryImpl): ProductRepository

    @Binds
    abstract fun bindPhotoSource(implementation: ContentProductPhotoSource): ProductPhotoSource
}
