package com.swyp.mangro.notification.model

sealed interface OwnerNotificationEvent {
    data class RefreshHome(val type: OwnerNotificationType) : OwnerNotificationEvent

    data class StockReconfirmationRequested(val key: String, val productId: Long) : OwnerNotificationEvent
}
