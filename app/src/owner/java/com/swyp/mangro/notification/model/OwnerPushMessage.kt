package com.swyp.mangro.notification.model

enum class OwnerNotificationType {
    NEW_HOLD_RECEIVED,
    STOCK_RECONFIRM_REQUEST,
    HOLD_UNCONFIRMED,
}

/** Only the Owner types in the server notification table are accepted. */
data class OwnerPushMessage(val type: OwnerNotificationType, val title: String, val body: String, val notificationId: Long? = null) {
    companion object {
        fun from(type: String?, title: String?, body: String?, notificationId: String? = null): OwnerPushMessage? {
            val ownerType = OwnerNotificationType.entries.firstOrNull { it.name == type } ?: return null
            if (title.isNullOrBlank() || body.isNullOrBlank()) return null
            return OwnerPushMessage(ownerType, title, body, notificationId?.toLongOrNull()?.takeIf { it > 0 })
        }
    }
}
