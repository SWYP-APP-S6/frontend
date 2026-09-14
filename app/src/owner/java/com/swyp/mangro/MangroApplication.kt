package com.swyp.mangro

import android.app.Application
import com.swyp.mangro.core.utils.NetworkConnectivityManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MangroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val key = getString(com.swyp.mangro.feature.auth.R.string.owner_kakao_native_key)
        if (key.isNotBlank()) com.kakao.sdk.common.KakaoSdk.init(this, key)
    }

    @Inject
    lateinit var networkConnectivityManager: NetworkConnectivityManager
}
