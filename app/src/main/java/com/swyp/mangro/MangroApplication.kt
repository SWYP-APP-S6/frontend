package com.swyp.mangro

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.swyp.mangro.core.utils.NetworkConnectivityManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MangroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val nativeAppKey = BuildConfig.KAKAO_NATIVE_APP_KEY
        if (nativeAppKey.isNotBlank()) {
            KakaoSdk.init(this, nativeAppKey, loggingEnabled = false)
        }
    }

    @Inject
    lateinit var networkConnectivityManager: NetworkConnectivityManager
}
