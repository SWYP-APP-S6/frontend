package com.swyp.mangro.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Pending foreground presentation; the system notification remains available after process death. */
object OwnerStockReconfirmationRequests {
    private val _pending = MutableStateFlow<OwnerStockReconfirmationRequest?>(null)
    val pending = _pending.asStateFlow()
    fun offer(request: OwnerStockReconfirmationRequest) {
        _pending.value = request
    }
    fun consume(request: OwnerStockReconfirmationRequest) {
        _pending.compareAndSet(request, null)
    }
    fun clear() {
        _pending.value = null
    }
}

data class OwnerStockReconfirmationRequest(val key: String, val productId: Long)
