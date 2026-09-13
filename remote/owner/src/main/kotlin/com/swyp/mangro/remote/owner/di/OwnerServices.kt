package com.swyp.mangro.remote.owner.di

import com.swyp.mangro.remote.owner.service.HoldService
import com.swyp.mangro.remote.owner.service.HomeService
import com.swyp.mangro.remote.owner.service.ProductService
import com.swyp.mangro.remote.owner.service.StoreService
import retrofit2.Retrofit

/** Service instances share the caller-supplied network configuration. */
class OwnerServices(retrofit: Retrofit) {
    val hold: HoldService = retrofit.create(HoldService::class.java)
    val home: HomeService = retrofit.create(HomeService::class.java)
    val product: ProductService = retrofit.create(ProductService::class.java)
    val store: StoreService = retrofit.create(StoreService::class.java)
}
