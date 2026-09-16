package com.swyp.mangro.feature.auth.login

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import javax.inject.Inject

class KakaoLoginLauncher @Inject constructor() {
    fun login(context: Context, onResult: (KakaoLoginResult, String?) -> Unit) {
        if (!KakaoSdk.isInitialized) {
            onResult(KakaoLoginResult.NotConfigured, null)
            return
        }
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            onResult(
                when {
                    error is ClientError && error.reason == ClientErrorCause.Cancelled -> KakaoLoginResult.Cancelled
                    error != null -> KakaoLoginResult.Failed
                    token != null -> KakaoLoginResult.Success
                    else -> KakaoLoginResult.Failed
                },
                token?.accessToken,
            )
        }
        fun loginWithAccount() {
            try {
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            } catch (error: Exception) {
                callback(null, error)
            }
        }
        try {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    if (error != null && !(error is ClientError && error.reason == ClientErrorCause.Cancelled)) {
                        loginWithAccount()
                    } else {
                        callback(token, error)
                    }
                }
            } else {
                loginWithAccount()
            }
        } catch (error: Exception) {
            callback(null, error)
        }
    }
}
