package com.swyp.mangro.data.consumer.home.repository

import com.swyp.mangro.data.consumer.home.model.ActiveHold
import com.swyp.mangro.data.consumer.home.model.MyLocation
import com.swyp.mangro.data.consumer.home.model.NearbyProducts
import com.swyp.mangro.data.consumer.home.model.NearbyStores
import com.swyp.mangro.data.consumer.home.model.ProductSortOption
import com.swyp.mangro.data.consumer.home.model.StoreDetail
import kotlinx.coroutines.flow.Flow

interface ConsumerHomeRepository {
    fun fetchMyLocation(): Flow<Result<MyLocation?>>
    fun setMyLocation(regionName: String, latitude: Double, longitude: Double): Flow<Result<MyLocation>>
    fun fetchNearbyStores(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): Flow<Result<NearbyStores>>
    fun fetchStoreProducts(storeId: Long, lat: Double? = null, lng: Double? = null): Flow<Result<StoreDetail>>
    fun fetchNearbyProducts(
        lat: Double,
        lng: Double,
        category: String? = null,
        sort: ProductSortOption? = null,
        radiusMeters: Int? = null,
        page: Int? = null,
        size: Int? = null,
    ): Flow<Result<NearbyProducts>>
    fun fetchActiveHold(): Flow<Result<ActiveHold?>>
}
