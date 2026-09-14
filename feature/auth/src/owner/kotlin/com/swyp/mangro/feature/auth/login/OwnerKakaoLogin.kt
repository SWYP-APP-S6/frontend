package com.swyp.mangro.feature.auth.login

import android.content.Context
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.swyp.mangro.feature.auth.R

internal fun launchOwnerKakaoLogin(context: Context, onAction: (LoginUiAction) -> Unit) {
    val key = context.getString(R.string.owner_kakao_native_key)
    if (key.isBlank()) {
        onAction(LoginUiAction.KakaoLoginFailed(cancelled = false))
        return
    }
    try {
        KakaoSdk.init(context.applicationContext, key)
        val client = UserApiClient.instance
        fun accountLogin() {
            client.loginWithKakaoAccount(context) { token, error ->
                when {
                    token != null -> onAction(LoginUiAction.KakaoTokenReceived(token.accessToken))
                    else -> onAction(LoginUiAction.KakaoLoginFailed(error is ClientError && error.reason == ClientErrorCause.Cancelled))
                }
            }
        }
        if (client.isKakaoTalkLoginAvailable(context)) {
            client.loginWithKakaoTalk(context) { token, error ->
                when {
                    token != null -> onAction(LoginUiAction.KakaoTokenReceived(token.accessToken))
                    error is ClientError && error.reason == ClientErrorCause.Cancelled -> onAction(LoginUiAction.KakaoLoginFailed(cancelled = true))
                    else -> accountLogin()
                }
            }
        } else {
            accountLogin()
        }
    } catch (error: IllegalStateException) {
        onAction(LoginUiAction.KakaoLoginFailed(cancelled = false))
    }
}
