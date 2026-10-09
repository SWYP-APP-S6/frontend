package com.swyp.mangro.notification.provider

import com.swyp.mangro.notification.model.OwnerNotificationEvent
import com.swyp.mangro.notification.model.OwnerNotificationType
import com.swyp.mangro.notification.model.OwnerPushMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onSubscription

/** 알림 타입에 따른 화면 갱신과 재고 다이얼로그 요청을 전달한다. */
class OwnerNotificationProvider {
    private val events = MutableSharedFlow<OwnerNotificationEvent>(extraBufferCapacity = 1)
    private val latestRefresh = MutableStateFlow<OwnerNotificationEvent.RefreshHome?>(null)
    private val pendingStock = MutableStateFlow<OwnerNotificationEvent.StockReconfirmationRequested?>(null)

    suspend fun onNotificationReceived(push: OwnerPushMessage, messageId: String) {
        val event: OwnerNotificationEvent = when (push.type) {
            OwnerNotificationType.STOCK_RECONFIRM_REQUEST -> {
                val productId = push.productId ?: return

                OwnerNotificationEvent
                    .StockReconfirmationRequested("${push.type}:$messageId:$productId", productId)
                    .also { pendingStock.value = it }
            }

            OwnerNotificationType.NEW_HOLD_RECEIVED, OwnerNotificationType.HOLD_EXPIRED -> {
                OwnerNotificationEvent
                    .RefreshHome(push.type)
                    .also { latestRefresh.value = it }
            }

            else -> return
        }

        events.emit(event)
    }

    /** 구독 등록 후 대기 중인 요청을 전달해 구독 시작 전후에 들어온 알림도 처리한다. */
    fun observeEvents(): Flow<OwnerNotificationEvent> = events.onSubscription {
        pendingStock.value?.let { emit(it) }
        latestRefresh.value?.let { emit(it) }
    }

    fun consumeStockReconfirmation(request: OwnerNotificationEvent.StockReconfirmationRequested) {
        pendingStock.compareAndSet(request, null)
    }

    fun clearPendingRequests() {
        pendingStock.value = null
    }
}
