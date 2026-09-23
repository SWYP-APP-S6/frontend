package com.swyp.mangro.feature.consumer.myinfo

import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class KakaoUnlinker @Inject constructor() {
    suspend fun unlink() {
        if (!KakaoSdk.isInitialized) return
        suspendCancellableCoroutine { continuation ->
            try {
                UserApiClient.instance.unlink { _ ->
                    if (continuation.isActive) continuation.resume(Unit)
                }
            } catch (_: Exception) {
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
    }
}
