package com.swyp.mangro.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkConnectivityModule {
    @Binds
    @Singleton
    abstract fun bindNetworkConnectivityManager(impl: NetworkConnectivityManagerImpl): NetworkConnectivityManager
}

enum class NetworkConnectivityStatus {
    WIFI,
    MOBILE_DATA,
    OTHERS,
    NONE,
}

interface NetworkConnectivityManager {
    val connectivityStatus: StateFlow<NetworkConnectivityStatus>

    fun isConnected(): Boolean
}

@Singleton
class NetworkConnectivityManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : NetworkConnectivityManager {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    private val _connectivityStatus = MutableStateFlow(NetworkConnectivityStatus.NONE)
    override val connectivityStatus = _connectivityStatus.asStateFlow()

    private val connectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            _connectivityStatus.update { NetworkConnectivityStatus.NONE }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            _connectivityStatus.update { networkCapabilities.toStatus() }
        }
    }

    private val callbackHandler = Handler(Looper.getMainLooper())

    init {
        connectivityManager.registerDefaultNetworkCallback(connectivityManagerCallback, callbackHandler)
        _connectivityStatus.update { connectivityManager.activeNetwork.toStatus() }
    }

    override fun isConnected(): Boolean = (_connectivityStatus.value != NetworkConnectivityStatus.NONE)

    private fun Network?.toStatus(): NetworkConnectivityStatus {
        if (this == null) return NetworkConnectivityStatus.NONE

        return connectivityManager.getNetworkCapabilities(this).toStatus()
    }

    private fun NetworkCapabilities?.toStatus(): NetworkConnectivityStatus = when {
        this?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkConnectivityStatus.WIFI
        this?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkConnectivityStatus.MOBILE_DATA
        this?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkConnectivityStatus.OTHERS
        else -> NetworkConnectivityStatus.NONE
    }
}
