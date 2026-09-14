package com.swyp.mangro.remote.consumer.di

import com.swyp.mangro.remote.consumer.service.HealthService
import com.swyp.mangro.remote.consumer.service.HoldService
import com.swyp.mangro.remote.consumer.service.NotificationService
import com.swyp.mangro.remote.consumer.service.ProductService
import com.swyp.mangro.remote.consumer.service.RecipeService
import com.swyp.mangro.remote.consumer.service.StoreService
import com.swyp.mangro.remote.consumer.service.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ConsumerServiceModule {
    @Provides
    @Singleton
    fun provideHealthService(retrofit: Retrofit): HealthService = retrofit.create(HealthService::class.java)

    @Provides
    @Singleton
    fun provideHoldService(retrofit: Retrofit): HoldService = retrofit.create(HoldService::class.java)

    @Provides
    @Singleton
    fun provideNotificationService(retrofit: Retrofit): NotificationService = retrofit.create(NotificationService::class.java)

    @Provides
    @Singleton
    fun provideProductService(retrofit: Retrofit): ProductService = retrofit.create(ProductService::class.java)

    @Provides
    @Singleton
    fun provideRecipeService(retrofit: Retrofit): RecipeService = retrofit.create(RecipeService::class.java)

    @Provides
    @Singleton
    fun provideStoreService(retrofit: Retrofit): StoreService = retrofit.create(StoreService::class.java)

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService = retrofit.create(UserService::class.java)
}
