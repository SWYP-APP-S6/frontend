package com.swyp.mangro.remote.owner.service

import retrofit2.Retrofit

/** Service instances share the caller-supplied network configuration. */
class OwnerServices(retrofit: Retrofit) {
    val hold: HoldService = retrofit.create(HoldService::class.java)
    val home: HomeService = retrofit.create(HomeService::class.java)
    val product: ProductService = retrofit.create(ProductService::class.java)
    val store: StoreService = retrofit.create(StoreService::class.java)
}
