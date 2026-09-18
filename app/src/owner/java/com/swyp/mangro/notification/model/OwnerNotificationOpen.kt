package com.swyp.mangro.notification.model

/** Payload has no target IDs; navigation uses the notification type rather than guessing a detail ID. */
data class OwnerNotificationOpen(val type: OwnerNotificationType, val notificationId: Long?) {
    companion object {
        fun from(type: String?, notificationId: String?): OwnerNotificationOpen? {
            val ownerType = OwnerNotificationType.entries.firstOrNull { it.name == type } ?: return null
            return OwnerNotificationOpen(ownerType, notificationId?.toLongOrNull()?.takeIf { it > 0 })
        }
    }
}
