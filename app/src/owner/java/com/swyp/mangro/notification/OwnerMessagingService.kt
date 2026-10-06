package com.swyp.mangro.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.swyp.core.local.store.AuthStore
import com.swyp.mangro.MainActivity
import com.swyp.mangro.R
import com.swyp.mangro.notification.model.OwnerPushMessage
import com.swyp.mangro.notification.provider.OwnerNotificationProvider
import com.swyp.mangro.notification.worker.OwnerTokenWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class OwnerMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var authStore: AuthStore

    @Inject
    lateinit var ownerNotificationProvider: OwnerNotificationProvider

    override fun onNewToken(token: String) {
        OwnerTokenWorker.enqueue(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val push = OwnerPushMessage.from(
            message.data["type"],
            message.notification?.title ?: message.data["title"],
            message.notification?.body ?: message.data["body"],
            message.data["notificationId"],
            message.data["deepLink"],
        ) ?: return

        val messageId = push.notificationId?.toString() ?: message.messageId ?: UUID.randomUUID().toString()
        val signedIn = runBlocking {
            authStore.authKey
                .map { authKey -> authKey != null }
                .catch { emit(false) }
                .first()
        }

        if (!signedIn) return
        runBlocking {
            ownerNotificationProvider.onNotificationReceived(push, messageId)
        }
        showNotification(this, push, messageId)
    }

    companion object {
        internal const val CHANNEL_ID = "owner-store-alerts"

        internal fun createNotificationChannel(context: Context) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, context.getString(R.string.owner_notification_channel), NotificationManager.IMPORTANCE_DEFAULT),
            )
        }

        internal fun showNotification(context: Context, message: OwnerPushMessage, messageId: String) {
            createNotificationChannel(context)
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
            val manager = NotificationManagerCompat.from(context)
            if (!manager.areNotificationsEnabled()) return
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("type", message.type.name)
                message.notificationId?.let { putExtra("notificationId", it.toString()) }
                message.deepLink?.let {
                    putExtra("deepLink", it)
                    data = it.toUri()
                }
            }
            val pending = PendingIntent.getActivity(context, messageId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_owner_notification)
                .setContentTitle(message.title)
                .setContentText(message.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .build()
            manager.notify(messageId, 0, notification)
        }
    }
}
