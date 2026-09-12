package com.swyp.mangro.core.utils

internal fun remainingPickupMinutes(deadlineMillis: Long, nowMillis: Long): Long {
    val remaining = (deadlineMillis - nowMillis).coerceAtLeast(0L)
    return remaining / 60_000 + if (remaining % 60_000 > 0) 1 else 0
}
