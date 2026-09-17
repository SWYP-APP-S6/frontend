package com.swyp.mangro.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Pending foreground presentation; the system notification remains available after process death. */
object OwnerStockReconfirmationRequests {
    private val _pending = MutableStateFlow<String?>(null)
    val pending = _pending.asStateFlow()
    fun offer(key: String) {
        _pending.value = key
    }
    fun consume(key: String) {
        _pending.compareAndSet(key, null)
    }
    fun clear() {
        _pending.value = null
    }
}
