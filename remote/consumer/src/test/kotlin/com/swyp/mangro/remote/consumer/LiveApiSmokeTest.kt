package com.swyp.mangro.remote.consumer

import com.swyp.mangro.core.network.BearerTokenInterceptor
import com.swyp.mangro.core.network.NetworkClient
import com.swyp.mangro.core.network.readHttpError
import com.swyp.mangro.remote.auth.di.AuthServices
import com.swyp.mangro.remote.auth.model.ExchangeConsumerCodeRequest
import com.swyp.mangro.remote.auth.model.ExchangeOwnerCodeRequest
import com.swyp.mangro.remote.auth.model.IssueGuestTokenRequest
import com.swyp.mangro.remote.auth.model.LogoutRequest
import com.swyp.mangro.remote.auth.model.RefreshUserAuthKeyRequest
import com.swyp.mangro.remote.auth.model.RegisterUserRequest
import com.swyp.mangro.remote.auth.model.VerifyConsumerKakaoTokenAndLoginRequest
import com.swyp.mangro.remote.auth.model.VerifyOwnerKakaoTokenAndLoginRequest
import com.swyp.mangro.remote.consumer.di.ConsumerServices
import com.swyp.mangro.remote.owner.di.OwnerServices
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import retrofit2.Response

/** Explicit opt-in only. One guest issuance followed by read-only requests; no account mutations. */
class LiveApiSmokeTest {
    @Test
    fun guestBrowseAndMemberBoundary() {
        assumeTrue(System.getProperty("mangro.liveApi") == "true")
        runBlocking {
            val publicClient = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build()
            val publicRetrofit = NetworkClient.create(client = publicClient)
            val publicServices = ConsumerServices(publicRetrofit)
            success("GET /ping", publicServices.health.checkHealth())

            val installId = UUID.nameUUIDFromBytes("mangro-remote-api-live-smoke-v1".toByteArray()).toString()
            val issued = AuthServices(publicRetrofit).auth.issueGuestToken(IssueGuestTokenRequest(installId = installId))
            val token = success("POST /auth/guest", issued).data.accessToken
            assertTrue("Guest response must contain a token", token.isNotBlank())
            // Keep the token in memory; never log the response body or auth header.
            val client = publicClient.newBuilder().addInterceptor(BearerTokenInterceptor { token }).build()
            val consumer = ConsumerServices(NetworkClient.create(client = client))
            val categories = success("GET /recipes/categories", consumer.recipe.fetchCategories())
            assertEquals("OK", categories.code)
            val recipes = success("GET /recipes?page=0&size=2&sort=id,asc", consumer.recipe.fetchRecipes(page = 0, size = 2, sort = listOf("id,asc")))
            assertEquals(0, recipes.data.page)
            assertEquals(2, recipes.data.propertySize)
            assertTrue("Live recipe dataset must be nonempty", recipes.data.content.isNotEmpty())
            assertTrue("v2 sample recipe difficulty must be populated", recipes.data.content.all { it.difficulty != null })
            assertTrue("v2 sample recipe cookTimeMinutes must be populated", recipes.data.content.all { it.cookTimeMinutes != null })
            println("LIVE recipe items=${recipes.data.content.size}, total=${recipes.data.totalElements}, v2 difficulty/cookTime populated")
            val recipe = success("GET /recipes/{id}", consumer.recipe.fetchRecipe(recipes.data.content.first().id))
            assertEquals("OK", recipe.code)
            val stores = success("GET /stores/nearby", consumer.store.fetchNearbyStores(37.56, 37.57, 126.97, 126.99))
            assertEquals("OK", stores.code)
            val products = success("GET /products/nearby", consumer.product.fetchNearbyProducts(37.5665, 126.9780, radiusMeters = 1000, page = 0, size = 2))
            assertEquals("OK", products.code)
            val denied = consumer.user.fetchMe()
            assertEquals("Guest member-only access", 403, denied.code())
            assertEquals("LOGIN_REQUIRED", denied.readHttpError()?.code)
            println("LIVE GET /users/me HTTP=403 code=LOGIN_REQUIRED")
            println("LIVE stores=${stores.data.stores.size}, products=${products.data.totalProductCount}")
            val storeId = stores.data.stores.firstOrNull()?.storeId ?: products.data.stores.content.firstOrNull()?.storeId
            if (storeId != null) {
                success("GET /stores/{storeId}/products", consumer.store.fetchStoreProducts(storeId, 37.5665, 126.9780))
            } else {
                println("LIVE SKIP GET /stores/{storeId}/products: no observed store ID")
            }
            val productId = products.data.stores.content.firstOrNull()?.products?.firstOrNull()?.id
            if (productId != null) {
                success("GET /products/{productId}", consumer.product.fetchProduct(productId, 37.5665, 126.9780))
            } else {
                println("LIVE SKIP GET /products/{productId}: no observed product ID")
            }
            val owner = OwnerServices(NetworkClient.create(client = client))
            val auth = AuthServices(publicRetrofit).auth
            val probes: List<Pair<String, suspend () -> Response<*>>> = listOf(
                "GET /users/me/location" to { consumer.user.fetchMyLocation() },
                "GET /holds" to { consumer.hold.fetchHolds(page = 0, size = 1) },
                "GET /holds/active" to { consumer.hold.fetchActiveHold() },
                "GET /holds/{holdId}" to { consumer.hold.fetchHold(0L) },
                "GET /notifications" to { consumer.notification.fetchNotifications(page = 0, size = 1) },
                "GET /owner/home" to { owner.home.fetchOwnerHome() },
                "GET /owner/stores/me" to { owner.store.fetchMyStore() },
                "GET /owner/products/{id}" to { owner.product.fetchMyProduct(0L) },
                "GET /owner/holds" to { owner.hold.fetchOwnerHolds(page = 0, size = 1) },
                "GET /owner/holds/{id}" to { owner.hold.fetchOwnerHold(0L) },
                "POST /auth/signup" to { auth.registerUser(RegisterUserRequest()) },
                "POST /auth/refresh" to { auth.refreshUserAuthKey(RefreshUserAuthKeyRequest()) },
                "POST /auth/logout" to { auth.logout(LogoutRequest()) },
                "POST /auth/consumer/kakao" to { auth.verifyConsumerKakaoTokenAndLogin(VerifyConsumerKakaoTokenAndLoginRequest()) },
                "POST /auth/owner/kakao" to { auth.verifyOwnerKakaoTokenAndLogin(VerifyOwnerKakaoTokenAndLoginRequest()) },
                "POST /auth/consumer/kakao/exchange" to { auth.exchangeConsumerCode(ExchangeConsumerCodeRequest()) },
                "POST /auth/owner/kakao/exchange" to { auth.exchangeOwnerCode(ExchangeOwnerCodeRequest()) },
            )
            val failures = mutableListOf<String>()
            for ((label, call) in probes) {
                val response = call()
                val error = response.readHttpError()
                println("LIVE NEGATIVE $label HTTP=${response.code()} code=${error?.code}")
                if (response.code() !in setOf(400, 401, 403) || error?.code.isNullOrBlank()) {
                    failures += "$label HTTP=${response.code()}"
                }
            }
            assertTrue("Unexpected negative responses: $failures", failures.isEmpty())
        }
    }

    private fun <T> success(label: String, response: Response<T>): T {
        println("LIVE $label HTTP=${response.code()}")
        assertTrue("$label HTTP=${response.code()}", response.isSuccessful)
        return checkNotNull(response.body()) { "$label empty response" }
    }
}
