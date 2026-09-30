package com.swyp.mangro.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.swyp.core.local.store.AuthStore
import com.swyp.mangro.MainActivity
import com.swyp.mangro.R
import com.swyp.mangro.notification.model.OwnerNotificationType
import com.swyp.mangro.notification.model.OwnerPushMessage
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class OwnerMessagingService : FirebaseMessagingService() {
    @Inject lateinit var authStore: AuthStore

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
        val signedIn = runBlocking(Dispatchers.IO) { runCatching { authStore.authKey.first() != null }.getOrDefault(false) }
        if (!signedIn) return
        val messageId = push.notificationId?.toString() ?: message.messageId ?: UUID.randomUUID().toString()
        if (push.type == OwnerNotificationType.STOCK_RECONFIRM_REQUEST) {
            push.productId?.let {
                _pendingStockReconfirmation.value = StockReconfirmationRequest("${push.type}:$messageId:$it", it)
            }
        }
        showNotification(this, push, messageId)
    }

    internal data class StockReconfirmationRequest(val key: String, val productId: Long)

    companion object {
        internal const val CHANNEL_ID = "owner-store-alerts"

        private val _pendingStockReconfirmation = MutableStateFlow<StockReconfirmationRequest?>(null)
        internal val pendingStockReconfirmation = _pendingStockReconfirmation.asStateFlow()

        internal fun consumeStockReconfirmation(request: StockReconfirmationRequest) {
            _pendingStockReconfirmation.compareAndSet(request, null)
        }

        internal fun clearStockReconfirmation() {
            _pendingStockReconfirmation.value = null
        }

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
                    data = Uri.parse(it)
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
