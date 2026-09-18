package com.swyp.mangro.notification

import android.util.Log
import com.swyp.mangro.BuildConfig

internal object OwnerNotificationLog {
    const val TAG = "OwnerNotification"

    fun debug(message: () -> String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message())
    }

    fun warn(error: Throwable? = null, message: () -> String) {
        if (!BuildConfig.DEBUG) return
        if (error == null) {
            Log.w(TAG, message())
        } else {
            Log.w(TAG, message(), error)
        }
    }
}
