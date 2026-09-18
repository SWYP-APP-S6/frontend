package com.swyp.mangro.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationProviderModule {
    @Binds
    @Singleton
    abstract fun bindLocationProvider(impl: LocationProviderImpl): LocationProvider
}

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
)

interface LocationProvider {
    /**
     * 기기의 현재 GPS 위치를 한 번 조회한다.
     * ACCESS_FINE_LOCATION 권한이 없거나 위치를 가져오지 못하면 null을 반환한다.
     */
    suspend fun fetchCurrentLocation(): DeviceLocation?

    /**
     * 좌표를 역지오코딩하여 동네 이름(예: "망원동")을 조회한다.
     * 변환에 실패하면 null을 반환한다.
     */
    suspend fun fetchRegionName(latitude: Double, longitude: Double): String?
}

@Singleton
class LocationProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : LocationProvider {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder by lazy { Geocoder(context, Locale.KOREA) }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun fetchCurrentLocation(): DeviceLocation? {
        val current = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            null,
        ).await()

        val location = current ?: fusedLocationClient.lastLocation.await()

        return location?.let { DeviceLocation(latitude = it.latitude, longitude = it.longitude) }
    }

    override suspend fun fetchRegionName(latitude: Double, longitude: Double): String? = try {
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            fetchAddressAsync(latitude, longitude)
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
        }
        // subLocality(예: 망원동) 우선, 없으면 locality(예: 마포구)로 대체
        address?.subLocality ?: address?.locality
    } catch (error: Exception) {
        null
    }

    private suspend fun fetchAddressAsync(latitude: Double, longitude: Double) = suspendCancellableCoroutine { continuation ->
        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
            continuation.resume(addresses.firstOrNull())
        }
    }
}
