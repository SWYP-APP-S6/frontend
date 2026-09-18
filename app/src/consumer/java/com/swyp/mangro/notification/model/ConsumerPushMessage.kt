package com.swyp.mangro.notification.model

data class ConsumerPushMessage(
    val type: String?,
    val title: String,
    val body: String,
    val deepLink: String?,
    val notificationId: Long? = null,
) {
    companion object {
        fun from(type: String?, title: String?, body: String?, deepLink: String?, notificationId: String? = null): ConsumerPushMessage? {
            if (title.isNullOrBlank() || body.isNullOrBlank()) return null
            return ConsumerPushMessage(type, title, body, deepLink, notificationId?.toLongOrNull()?.takeIf { it > 0 })
        }
    }
}
