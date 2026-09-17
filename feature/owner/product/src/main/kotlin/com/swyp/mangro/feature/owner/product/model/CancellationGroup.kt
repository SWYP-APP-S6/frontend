package com.swyp.mangro.feature.owner.product.model

data class CancellationTarget(val id: String, val customerName: String, val quantity: Int, val requestedAt: Long, val notificationsEnabled: Boolean = true)
data class CancellationGroup(val productId: String, val productName: String, val targets: List<CancellationTarget>, val requestPositions: Map<String, Int>)
