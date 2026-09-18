package com.swyp.mangro.notification.model

import java.net.URI

internal data class OwnerProductDeepLink(val value: String, val productId: Long)

internal fun parseOwnerProductDeepLink(value: String?): OwnerProductDeepLink? {
    val raw = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val uri = runCatching { URI(raw) }.getOrNull() ?: return null
    if (!uri.scheme.equals("mangro", ignoreCase = true) || !uri.host.equals("owner", ignoreCase = true)) return null
    if (uri.userInfo != null || uri.port != -1 || uri.rawQuery != null || uri.rawFragment != null) return null
    val productId = PRODUCT_PATH.matchEntire(uri.rawPath.orEmpty())?.groupValues?.get(1)?.toLongOrNull()?.takeIf { it > 0 } ?: return null
    return OwnerProductDeepLink(raw, productId)
}

private val PRODUCT_PATH = Regex("/products/([1-9][0-9]*)")
