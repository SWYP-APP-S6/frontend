package com.swyp.mangro.remote.consumer

import com.swyp.mangro.core.network.Constants
import com.swyp.mangro.core.network.di.NetworkModule
import com.swyp.mangro.core.network.error.readHttpError
import com.swyp.mangro.core.network.interceptor.AuthorizationInterceptor
import com.swyp.mangro.remote.auth.model.ExchangeConsumerCodeRequest
import com.swyp.mangro.remote.auth.model.ExchangeOwnerCodeRequest
import com.swyp.mangro.remote.auth.model.IssueGuestTokenRequest
import com.swyp.mangro.remote.auth.model.LogoutRequest
import com.swyp.mangro.remote.auth.model.RefreshUserAuthKeyRequest
import com.swyp.mangro.remote.auth.model.RegisterUserRequest
import com.swyp.mangro.remote.auth.model.VerifyConsumerKakaoTokenAndLoginRequest
import com.swyp.mangro.remote.auth.model.VerifyOwnerKakaoTokenAndLoginRequest
import com.swyp.mangro.remote.auth.service.AuthServices
import com.swyp.mangro.remote.consumer.serivce.ConsumerServices
import com.swyp.mangro.remote.owner.service.OwnerServices
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit

/** Explicit opt-in only. One guest issuance followed by read-only requests; no account mutations. */
class LiveApiSmokeTest {
    private val json = NetworkModule.provideNetworkJson()

    private fun createRetrofit(
        baseUrl: String = Constants.BASE_URL,
        client: OkHttpClient = OkHttpClient(),
    ): Retrofit = NetworkModule.provideRetrofit(client, json).newBuilder().baseUrl(baseUrl).build()

    @Test
    fun guestBrowseAndMemberBoundary() {
        assumeTrue(System.getProperty("mangro.liveApi") == "true")
        runBlocking {
            val publicClient = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build()
            val publicRetrofit = createRetrofit(client = publicClient)
            val publicServices = ConsumerServices(publicRetrofit)
            success("GET /ping", publicServices.health.checkHealth())

            val installId = UUID.nameUUIDFromBytes("mangro-remote-api-live-smoke-v1".toByteArray()).toString()
            val issued = AuthServices(publicRetrofit).auth.issueGuestToken(IssueGuestTokenRequest(installId = installId))
            val token = success("POST /auth/guest", issued).accessToken
            assertTrue("Guest response must contain a token", token.isNotBlank())
            // Keep the token in memory; never log the response body or auth header.
            val client = publicClient.newBuilder().addInterceptor(AuthorizationInterceptor { token }).build()
            val consumer = ConsumerServices(createRetrofit(client = client))
            success("GET /recipes/categories", consumer.recipe.fetchCategories())
            val recipes = success("GET /recipes?page=0&size=2&sort=id,asc", consumer.recipe.fetchRecipes(page = 0, size = 2, sort = listOf("id,asc")))
            assertEquals(0, recipes.page)
            assertEquals(2, recipes.propertySize)
            assertTrue("Live recipe dataset must be nonempty", recipes.content.isNotEmpty())
            assertTrue("v2 sample recipe difficulty must be populated", recipes.content.all { it.difficulty != null })
            assertTrue("v2 sample recipe cookTimeMinutes must be populated", recipes.content.all { it.cookTimeMinutes != null })
            println("LIVE recipe items=${recipes.content.size}, total=${recipes.totalElements}, v2 difficulty/cookTime populated")
            success("GET /recipes/{id}", consumer.recipe.fetchRecipe(recipes.content.first().id))
            val stores = success("GET /stores/nearby", consumer.store.fetchNearbyStores(37.56, 37.57, 126.97, 126.99))
            val products = success("GET /products/nearby", consumer.product.fetchNearbyProducts(37.5665, 126.9780, radiusMeters = 1000, page = 0, size = 2))
            val denied = consumer.user.fetchMe()
            assertEquals("Guest member-only access", 403, denied.code())
            assertEquals("LOGIN_REQUIRED", denied.readHttpError(json)?.code)
            println("LIVE GET /users/me HTTP=403 code=LOGIN_REQUIRED")
            println("LIVE stores=${stores.stores.size}, products=${products.totalProductCount}")
            val storeId = stores.stores.firstOrNull()?.storeId ?: products.stores.content.firstOrNull()?.storeId
            if (storeId != null) {
                success("GET /stores/{storeId}/products", consumer.store.fetchStoreProducts(storeId, 37.5665, 126.9780))
            } else {
                println("LIVE SKIP GET /stores/{storeId}/products: no observed store ID")
            }
            val productId = products.stores.content.firstOrNull()?.products?.firstOrNull()?.id
            if (productId != null) {
                success("GET /products/{productId}", consumer.product.fetchProduct(productId, 37.5665, 126.9780))
            } else {
                println("LIVE SKIP GET /products/{productId}: no observed product ID")
            }
            val owner = OwnerServices(createRetrofit(client = client))
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
                val error = response.readHttpError(json)
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
