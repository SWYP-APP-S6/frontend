package com.swyp.mangro.notification.model

/** Deep links are currently absent in the server payload; every accepted tap opens Owner home. */
data class OwnerNotificationOpen(val type: OwnerNotificationType, val notificationId: Long?) {
    companion object {
        fun from(type: String?, notificationId: String?): OwnerNotificationOpen? {
            val ownerType = OwnerNotificationType.entries.firstOrNull { it.name == type } ?: return null
            return OwnerNotificationOpen(ownerType, notificationId?.toLongOrNull()?.takeIf { it > 0 })
        }
    }
}
