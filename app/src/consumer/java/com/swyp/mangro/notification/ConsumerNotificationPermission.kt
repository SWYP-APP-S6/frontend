package com.swyp.mangro.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.firebase.FirebaseApp

@Composable
internal fun ConsumerNotificationPermission() {
    val context = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        ConsumerTokenWorker.enqueue(context)
    }
    LifecycleResumeEffect(Unit) {
        ConsumerNotificationDisplay.createChannel(context)
        if (FirebaseApp.initializeApp(context) != null) {
            val preferences = context.getSharedPreferences("consumer-notifications", Context.MODE_PRIVATE)
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                !preferences.getBoolean("permission-requested", false)
            ) {
                preferences.edit { putBoolean("permission-requested", true) }
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                ConsumerTokenWorker.enqueue(context)
            }
        }
        onPauseOrDispose {}
    }
}
