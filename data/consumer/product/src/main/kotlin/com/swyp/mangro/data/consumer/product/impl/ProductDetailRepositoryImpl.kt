package com.swyp.mangro.data.consumer.product.impl

import com.swyp.mangro.data.consumer.product.model.ProductDetail
import com.swyp.mangro.data.consumer.product.model.ProductRecipe
import com.swyp.mangro.data.consumer.product.model.ProductStore
import com.swyp.mangro.data.consumer.product.repository.ProductDetailRepository
import com.swyp.mangro.remote.consumer.model.RegisterHoldRequest
import com.swyp.mangro.remote.consumer.service.HoldService
import com.swyp.mangro.remote.consumer.service.ProductService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException

internal class ProductDetailRepositoryImpl @Inject constructor(
    private val productService: ProductService,
    private val holdService: HoldService,
) : ProductDetailRepository {

    override fun fetchProduct(productId: Long, lat: Double?, lng: Double?): Flow<Result<ProductDetail>> = request {
        val response = productService.fetchProduct(
            productId,
            lat?.roundToSixDecimals(),
            lng?.roundToSixDecimals(),
        )
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw HttpException(response)
        }
        val body = requireNotNull(response.body())
        ProductDetail(
            id = body.id,
            name = body.name,
            category = body.category.value,
            tags = body.tags,
            photoUrls = body.photoUrls,
            originalPrice = body.originalPrice,
            salePrice = body.salePrice,
            discountRate = body.discountRate.takeIf { it > 0 },
            availableQty = body.availableQty,
            status = body.status.value,
            holdButton = body.holdButton.value,
            myHoldId = body.myHoldId,
            store = ProductStore(
                id = body.store.id,
                name = body.store.name,
                address = body.store.address,
                addressDetail = body.store.addressDetail,
                phone = body.store.phone,
                distanceMeters = body.store.distanceMeters,
                walkingMinutes = body.store.walkingMinutes,
                businessCloseTime = body.store.businessCloseTime,
                latitude = body.store.latitude,
                longitude = body.store.longitude,
            ),
            recipes = body.recipes.map {
                ProductRecipe(
                    id = it.id,
                    title = it.title,
                    difficulty = it.difficulty?.value,
                    ingredientNames = it.ingredientNames.splitIngredientsIfNeeded(),
                )
            },
        )
    }

    override fun registerHold(productId: Long, qty: Int): Flow<Result<Long>> = request {
        val response = holdService.registerHold(
            RegisterHoldRequest(productId = productId, qty = qty),
        )
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("ProductDetailRepo", "registerHold HTTP ${response.code()}: $errorBody")
            throw HttpException(response)
        }
        requireNotNull(response.body()).id
    }

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
}

private fun Double.roundToSixDecimals(): Double = (this * 1_000_000).toLong() / 1_000_000.0

private fun List<String>.splitIngredientsIfNeeded(): List<String> = if (size == 1) {
    this[0]
        .split(Regex("[,()、/·]+"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
} else {
    this
}
