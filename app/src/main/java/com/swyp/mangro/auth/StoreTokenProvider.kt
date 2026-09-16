package com.swyp.mangro.auth

import com.swyp.core.local.model.AuthKey
import com.swyp.core.local.store.AuthStore
import com.swyp.mangro.core.network.error.readHttpError
import com.swyp.mangro.core.network.exception.TokenRefreshException
import com.swyp.mangro.core.network.provider.TokenProvider
import com.swyp.mangro.remote.auth.model.RefreshUserAuthKeyRequest
import com.swyp.mangro.remote.auth.service.TokenRefreshService
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * DataStore Flow를 한 번 읽어 OkHttp 동기 호출에 연결합니다. 메모리 토큰 캐시가 아닙니다.
 * 갱신은 직렬화하고, 요청 중 로그아웃·계정 변경이 발생하면 갱신 결과를 저장하지 않습니다.
 */
class StoreTokenProvider @Inject constructor(
    private val refreshService: TokenRefreshService,
    private val authStore: AuthStore,
    private val json: Json,
) : TokenProvider {
    private val refreshMutex = Mutex()
    private val inFlight = ConcurrentHashMap<String, CompletableFuture<String?>>()
    private var lastFailedToken: String? = null
    private var lastRefreshedToken: String? = null

    override fun accessToken(): String? = blocking { authStore.authKey.first()?.accessToken }

    override fun refreshAccessToken(failedAccessToken: String): String? = blocking {
        val attempt = CompletableFuture<String?>()
        val existing = inFlight.putIfAbsent(failedAccessToken, attempt)
        if (existing != null) return@blocking existing.get()
        try {
            val token = refreshMutex.withLock { refresh(failedAccessToken) }
            attempt.complete(token)
            token
        } catch (failure: Throwable) {
            attempt.completeExceptionally(failure)
            throw failure
        } finally {
            inFlight.remove(failedAccessToken, attempt)
        }
    }

    private suspend fun refresh(failedAccessToken: String): String? {
        val previous = authStore.authKey.first() ?: return null

        if (previous.accessToken != failedAccessToken) {
            return previous.accessToken.takeIf {
                lastFailedToken == failedAccessToken && lastRefreshedToken == it
            }
        }

        val response = refreshService.refresh(
            authorization = "Bearer ${previous.accessToken}",
            request = RefreshUserAuthKeyRequest(previous.refreshToken),
        )

        response.readHttpError(json)?.let { throw TokenRefreshException(it) }

        if (response.code() != 200) throw IOException("Unexpected token refresh response status")

        val tokens = response.body() ?: throw IOException("Missing token refresh response")
        if (tokens.accessToken.isBlank() || tokens.refreshToken.isBlank()) throw IOException("Incomplete token refresh response")

        val updated = AuthKey(tokens.accessToken, tokens.refreshToken)
        if (!authStore.replaceIfMatches(previous, updated)) return null

        lastFailedToken = failedAccessToken
        lastRefreshedToken = updated.accessToken

        return updated.accessToken
    }

    private fun <T> blocking(block: suspend () -> T): T = try {
        runBlocking { block() }
    } catch (exception: ExecutionException) {
        val cause = exception.cause
        if (cause is IOException) throw cause
        throw IOException("Token refresh failed", cause)
    } catch (exception: IOException) {
        throw exception
    } catch (exception: InterruptedException) {
        Thread.currentThread().interrupt()
        throw IOException("Authentication token access interrupted", exception)
    } catch (exception: Exception) {
        throw IOException("Unable to access authentication tokens", exception)
    }
}
