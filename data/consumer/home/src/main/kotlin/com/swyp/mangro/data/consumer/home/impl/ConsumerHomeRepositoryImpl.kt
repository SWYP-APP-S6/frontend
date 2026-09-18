package com.swyp.mangro.data.consumer.home.impl

import com.swyp.mangro.data.consumer.home.model.ActiveHold
import com.swyp.mangro.data.consumer.home.model.MyLocation
import com.swyp.mangro.data.consumer.home.model.NearbyProducts
import com.swyp.mangro.data.consumer.home.model.NearbyStore
import com.swyp.mangro.data.consumer.home.model.NearbyStoreGroup
import com.swyp.mangro.data.consumer.home.model.NearbyStores
import com.swyp.mangro.data.consumer.home.model.ProductSortOption
import com.swyp.mangro.data.consumer.home.model.StoreDetail
import com.swyp.mangro.data.consumer.home.model.StoreDetailProduct
import com.swyp.mangro.data.consumer.home.repository.ConsumerHomeRepository
import com.swyp.mangro.remote.consumer.model.RegionResponse
import com.swyp.mangro.remote.consumer.model.SellableProductResponse
import com.swyp.mangro.remote.consumer.model.SetMyLocationRequest
import com.swyp.mangro.remote.consumer.service.HoldService
import com.swyp.mangro.remote.consumer.service.ProductService
import com.swyp.mangro.remote.consumer.service.StoreService
import com.swyp.mangro.remote.consumer.service.UserService
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

internal class ConsumerHomeRepositoryImpl @Inject constructor(
    private val userService: UserService,
    private val storeService: StoreService,
    private val productService: ProductService,
    private val holdService: HoldService,
) : ConsumerHomeRepository {

    override fun fetchMyLocation(): Flow<Result<MyLocation?>> = request {
        val response = userService.fetchMyLocation()
        if (!response.isSuccessful) throw HttpException(response)
        response.body()?.location?.toMyLocation()
    }

    override fun setMyLocation(regionName: String, latitude: Double, longitude: Double): Flow<Result<MyLocation>> = request {
        val response = userService.setMyLocation(
            SetMyLocationRequest(
                latitude = latitude.roundToSixDecimals(),
                longitude = longitude.roundToSixDecimals(),
                regionName = regionName,
            ),
        )
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("ConsumerHomeRepo", "setMyLocation HTTP ${response.code()}: $errorBody")
            throw HttpException(response)
        }
        requireNotNull(response.body()?.location).toMyLocation()
    }

    override fun fetchNearbyStores(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): Flow<Result<NearbyStores>> = request {
        val response = storeService.fetchNearbyStores(
            minLat.roundToSixDecimals(),
            maxLat.roundToSixDecimals(),
            minLng.roundToSixDecimals(),
            maxLng.roundToSixDecimals(),
        )
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("ConsumerHomeRepo", "fetchNearbyStores HTTP ${response.code()}: $errorBody")
            throw HttpException(response)
        }
        val body = requireNotNull(response.body())
        NearbyStores(
            totalStoreCount = body.totalStoreCount,
            truncated = body.truncated,
            stores = body.stores.map {
                NearbyStore(it.storeId, it.name, it.latitude, it.longitude, it.sellableProductCount)
            },
        )
    }

    override fun fetchStoreProducts(storeId: Long, lat: Double?, lng: Double?): Flow<Result<StoreDetail>> = request {
        require(storeId > 0)
        val response = storeService.fetchStoreProducts(storeId, lat, lng)
        if (!response.isSuccessful) throw HttpException(response)
        val body = requireNotNull(response.body())
        StoreDetail(
            storeId = body.storeId,
            name = body.name,
            latitude = body.latitude,
            longitude = body.longitude,
            distanceMeters = body.distanceMeters,
            walkingMinutes = body.walkingMinutes,
            businessCloseTime = body.businessCloseTime,
            earliestPickupEndAtMillis = body.earliestPickupEndAt?.toMillisOrNull(),
            productCount = body.productCount,
            products = body.products.map { it.toDomain() },
        )
    }

    override fun fetchNearbyProducts(
        lat: Double,
        lng: Double,
        category: String?,
        sort: ProductSortOption?,
        radiusMeters: Int?,
        page: Int?,
        size: Int?,
    ): Flow<Result<NearbyProducts>> = request {
        val response = productService.fetchNearbyProducts(
            lat = lat,
            lng = lng,
            category = category?.let { c -> ProductService.CategoryFetchNearbyProducts.entries.first { it.value == c } },
            sort = sort?.let { s -> ProductService.SortFetchNearbyProducts.entries.first { it.value == s.name } },
            radiusMeters = radiusMeters,
            page = page,
            size = size,
        )
        if (!response.isSuccessful) throw HttpException(response)
        val body = requireNotNull(response.body())
        NearbyProducts(
            totalProductCount = body.totalProductCount,
            page = body.stores.page,
            totalPages = body.stores.totalPages,
            last = body.stores.last,
            storeGroups = body.stores.content.map { group ->
                NearbyStoreGroup(
                    storeId = group.storeId,
                    storeName = group.storeName,
                    distanceMeters = group.distanceMeters,
                    walkingMinutes = group.walkingMinutes,
                    productCount = group.productCount,
                    hasMoreProducts = group.hasMoreProducts,
                    earliestPickupEndAtMillis = group.earliestPickupEndAt.toMillisOrNull(),
                    products = group.products.map { it.toDomain() },
                )
            },
        )
    }

    override fun fetchActiveHold(): Flow<Result<ActiveHold?>> = request {
        val response = holdService.fetchActiveHold()
        if (!response.isSuccessful) throw HttpException(response)
        val hold = response.body()?.hold ?: return@request null
        if (hold.status != com.swyp.mangro.remote.consumer.model.HoldDetailResponse.Status.HOLDING) return@request null
        ActiveHold(
            holdId = hold.id,
            storeName = hold.store.name,
            firstItemName = hold.items.firstOrNull()?.name.orEmpty(),
            totalQty = hold.totalQty,
            heldAtMillis = hold.heldAt.toMillisOrNull() ?: 0L,
            expiresAtMillis = hold.expiresAt.toMillisOrNull() ?: 0L,
        )
    }
}

private fun SellableProductResponse.toDomain(): StoreDetailProduct = StoreDetailProduct(
    id = id,
    name = name,
    photoUrl = photoUrl,
    category = category.value,
    originalPrice = originalPrice,
    salePrice = salePrice,
    discountRate = discountRate.takeIf { it > 0 },
    availableQty = availableQty,
    pickupEndAtMillis = pickupEndAt.toMillisOrNull(),
)

private fun RegionResponse.toMyLocation(): MyLocation = MyLocation(
    regionName = regionName,
    latitude = latitude,
    longitude = longitude,
    updatedAtMillis = updatedAt.toMillisOrNull(),
)

private fun String.toMillisOrNull(): Long? = takeIf { it.isNotBlank() }?.let { OffsetDateTime.parse(it).toInstant().toEpochMilli() }

private fun Double.roundToSixDecimals(): Double = (this * 1_000_000).toLong() / 1_000_000.0

private fun <T> request(block: suspend () -> T): Flow<Result<T>> = flow {
    val result = try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
    emit(result)
}.flowOn(Dispatchers.IO)
