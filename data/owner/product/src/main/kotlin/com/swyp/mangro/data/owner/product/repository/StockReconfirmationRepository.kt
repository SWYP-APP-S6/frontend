package com.swyp.mangro.data.owner.product.repository

import com.swyp.mangro.remote.owner.model.AnswerStockReconfirmRequest
import com.swyp.mangro.remote.owner.service.ProductService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class StockReconfirmation(val productId: Long, val name: String, val quantity: Int)

class StockReconfirmationRepository @Inject constructor(private val service: ProductService) {
    suspend fun fetch(productId: Long): Result<StockReconfirmation?> = request {
        require(productId > 0)
        val response = service.fetchMyProduct(productId)
        if (!response.isSuccessful) throw HttpException(response)
        val product = requireNotNull(response.body())
        if (product.reconfirmPending && product.stockEditable) StockReconfirmation(productId, product.name, product.stockQty) else null
    }

    suspend fun answer(productId: Long, confirmed: Boolean): Result<Unit> = request {
        require(productId > 0)
        val response = service.answerStockReconfirm(productId, AnswerStockReconfirmRequest(confirmed))
        if (!response.isSuccessful) throw HttpException(response)
        requireNotNull(response.body())
        Unit
    }

    private suspend fun <T> request(block: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
