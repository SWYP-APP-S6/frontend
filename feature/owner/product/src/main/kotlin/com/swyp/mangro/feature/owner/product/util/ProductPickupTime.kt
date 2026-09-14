package com.swyp.mangro.feature.owner.product.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal fun pickupTimeOptions(openingTime: String, closingTime: String, now: LocalTime): List<String> {
    val opening = runCatching { LocalTime.parse(openingTime) }.getOrNull() ?: return emptyList()
    val closing = runCatching { LocalTime.parse(closingTime) }.getOrNull() ?: return emptyList()
    if (!closing.isAfter(opening)) return emptyList()
    return ((0..23).map { LocalTime.of(it, 0) } + closing)
        .distinct()
        .filter { it.isAfter(now) && !it.isBefore(opening) && !it.isAfter(closing) }
        .sorted()
        .map { it.format(DateTimeFormatter.ofPattern("HH:mm")) }
}
