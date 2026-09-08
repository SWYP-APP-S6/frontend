package com.swyp.mangro

import android.app.Application
import com.swyp.mangro.core.utils.NetworkConnectivityManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MangroApplication : Application() {
    @Inject
    lateinit var networkConnectivityManager: NetworkConnectivityManager
}
