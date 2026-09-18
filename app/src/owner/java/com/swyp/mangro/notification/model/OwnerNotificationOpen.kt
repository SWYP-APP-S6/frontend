package com.swyp.mangro.notification.model

data class OwnerNotificationOpen(
    val type: OwnerNotificationType,
    val notificationId: Long?,
    val productId: Long?,
) {
    val key: String = "${type.name}:${notificationId ?: "none"}:${productId ?: "none"}"

    companion object {
        fun from(type: String?, notificationId: String?, deepLink: String? = null): OwnerNotificationOpen? {
            val ownerType = OwnerNotificationType.entries.firstOrNull { it.name == type } ?: return null
            val productId = if (ownerType == OwnerNotificationType.STOCK_RECONFIRM_REQUEST) {
                parseOwnerProductDeepLink(deepLink)?.productId
            } else {
                null
            }
            return OwnerNotificationOpen(
                ownerType,
                notificationId?.toLongOrNull()?.takeIf { it > 0 },
                productId,
            )
        }
    }
}
