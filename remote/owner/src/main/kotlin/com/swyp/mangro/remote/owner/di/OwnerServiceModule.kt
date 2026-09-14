package com.swyp.mangro.remote.owner.di

import com.swyp.mangro.remote.owner.service.HoldService
import com.swyp.mangro.remote.owner.service.HomeService
import com.swyp.mangro.remote.owner.service.ProductService
import com.swyp.mangro.remote.owner.service.StoreService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object OwnerServiceModule {
    @Provides
    @Singleton
    fun provideHoldService(retrofit: Retrofit): HoldService = retrofit.create(HoldService::class.java)

    @Provides
    @Singleton
    fun provideHomeService(retrofit: Retrofit): HomeService = retrofit.create(HomeService::class.java)

    @Provides
    @Singleton
    fun provideProductService(retrofit: Retrofit): ProductService = retrofit.create(ProductService::class.java)

    @Provides
    @Singleton
    fun provideStoreService(retrofit: Retrofit): StoreService = retrofit.create(StoreService::class.java)
}
