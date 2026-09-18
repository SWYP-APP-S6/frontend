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
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.firebase.FirebaseApp

@Composable
internal fun OwnerNotificationPermission() {
    val context = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        OwnerTokenWorker.enqueue(context)
    }
    LifecycleResumeEffect(Unit) {
        OwnerNotificationDisplay.createChannel(context)
        if (FirebaseApp.initializeApp(context) != null) {
            val preferences = context.getSharedPreferences("owner-notifications", Context.MODE_PRIVATE)
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                !preferences.getBoolean("permission-requested", false)
            ) {
                preferences.edit().putBoolean("permission-requested", true).apply()
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                OwnerTokenWorker.enqueue(context)
            }
        }
        onPauseOrDispose {}
    }
}
