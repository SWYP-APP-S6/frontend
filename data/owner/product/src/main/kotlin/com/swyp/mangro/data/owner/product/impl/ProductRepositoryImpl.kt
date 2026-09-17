package com.swyp.mangro.data.owner.product.impl

import com.swyp.mangro.data.owner.product.model.ProductRegistration
import com.swyp.mangro.data.owner.product.model.RegisteredProduct
import com.swyp.mangro.data.owner.product.repository.ProductRepository
import com.swyp.mangro.remote.owner.model.RegisterProductRequest
import com.swyp.mangro.remote.owner.service.ProductService
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import retrofit2.Response

internal class ProductRepositoryImpl @Inject constructor(
    private val service: ProductService,
    private val photos: ProductPhotoSource,
) : ProductRepository {
    override fun register(product: ProductRegistration) = flow {
        val result = try {
            require(product.name.isNotBlank() && product.name.length <= 30)
            require(product.quantity > 0 && product.originalPrice > 0 && product.salePrice in 1..product.originalPrice)
            require(product.ingredientTags.size <= 5)
            val category = RegisterProductRequest.Category.valueOf(if (product.category == "PREPARED_FOOD") "SIDE_DISH" else product.category)
            OffsetDateTime.parse(product.pickupEndAt)

            // Upload only the first selection. No selection means no upload request.
            val photoUrl = product.photos.firstOrNull()?.let {
                service.uploadProductPhoto(photos.part(it)).bodyOrThrow().photoUrl.also { url -> require(url.isNotBlank()) }
            }

            val body = service.registerProduct(
                RegisterProductRequest(
                    name = product.name.trim(),
                    category = category,
                    initialQty = product.quantity,
                    originalPrice = product.originalPrice,
                    salePrice = product.salePrice,
                    photoUrl = photoUrl.orEmpty(),
                    ingredientTags = product.ingredientTags,
                    pickupEndAt = product.pickupEndAt,
                ),
            ).bodyOrThrow()
            require(body.id > 0)
            Result.success(
                RegisteredProduct(
                    body.id,
                    body.name,
                    body.photoUrl,
                    body.originalPrice,
                    body.salePrice,
                    body.initialQty,
                    body.stockQty,
                    body.heldQty,
                    Math.toIntExact(body.completedQty),
                    body.pickupEndAt,
                ),
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
        emit(result)
    }.flowOn(Dispatchers.IO)
}

private fun <T> Response<T>.bodyOrThrow(): T {
    if (!isSuccessful) throw HttpException(this)
    return requireNotNull(body())
}
