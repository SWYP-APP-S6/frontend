package com.swyp.mangro.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.swyp.core.local.store.AuthStore
import com.swyp.mangro.notification.model.OwnerPushMessage
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
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
        ) ?: return
        val signedIn = runBlocking(Dispatchers.IO) { runCatching { authStore.authKey.first() != null }.getOrDefault(false) }
        if (!signedIn) return
        OwnerNotificationDisplay.show(this, push, push.notificationId?.toString() ?: message.messageId ?: UUID.randomUUID().toString())
    }
}
