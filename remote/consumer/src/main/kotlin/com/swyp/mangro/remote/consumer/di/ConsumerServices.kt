package com.swyp.mangro.remote.consumer.di

import com.swyp.mangro.remote.consumer.service.HealthService
import com.swyp.mangro.remote.consumer.service.HoldService
import com.swyp.mangro.remote.consumer.service.NotificationService
import com.swyp.mangro.remote.consumer.service.ProductService
import com.swyp.mangro.remote.consumer.service.RecipeService
import com.swyp.mangro.remote.consumer.service.StoreService
import com.swyp.mangro.remote.consumer.service.UserService
import retrofit2.Retrofit

/** Service instances share the caller-supplied network configuration. */
class ConsumerServices(retrofit: Retrofit) {
    val health: HealthService = retrofit.create(HealthService::class.java)
    val hold: HoldService = retrofit.create(HoldService::class.java)
    val notification: NotificationService = retrofit.create(NotificationService::class.java)
    val product: ProductService = retrofit.create(ProductService::class.java)
    val recipe: RecipeService = retrofit.create(RecipeService::class.java)
    val store: StoreService = retrofit.create(StoreService::class.java)
    val user: UserService = retrofit.create(UserService::class.java)
}
