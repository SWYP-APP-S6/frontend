package com.swyp.mangro.feature.owner.product.model

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestItem
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestStatus
import com.swyp.mangro.data.owner.product.model.HoldStatus
import com.swyp.mangro.data.owner.product.model.ManagedHold
import com.swyp.mangro.data.owner.product.model.ManagedProduct

internal fun ManagedProduct.presentation() = OwnerProductModel(
    id = id.toString(), name = name, photos = listOf(photoUrl).filter { it.isNotBlank() },
    originalPrice = originalPrice, salePrice = salePrice, initialQuantity = initialQuantity,
    remainingQuantity = stockQuantity, reservedQuantity = activeHoldQuantity, pickedUpQuantity = completedQuantity,
    pickupEndTime = pickupDate(pickupEndAt, "M월 d일 HH:mm"),
    tagsResolved = ingredientTags.isEmpty(),
    serverShortfall = shortfallQuantity, serverAvailable = availableQuantity,
    stockEditable = stockEditable, minAdjustableQuantity = minAdjustableQuantity,
)
internal fun HoldStatus.presentation() = when (this) {
    HoldStatus.HOLDING -> PickupStatus.WAITING
    HoldStatus.COMPLETED -> PickupStatus.COMPLETED
    HoldStatus.EXPIRED -> PickupStatus.EXPIRED
    HoldStatus.CANCELED_BY_OWNER -> PickupStatus.UNAVAILABLE
    HoldStatus.CANCELED_BY_USER -> PickupStatus.CANCELED
}
internal fun ManagedHold.presentation(now: Long, offset: Long, busy: Boolean): OwnerPickupModel {
    val active = status == HoldStatus.HOLDING && now < expiresAt
    return OwnerPickupModel(
        productId = productId.toString(),
        request = OwnerPickupRequestItem(
            id = id.toString(), requestedAt = pickupDate(heldAt), consumerName = nickname,
            pickupDeadlineText = "${pickupDate(expiresAt, "H시 mm분")}까지 픽업 예정",
            productName = productName, quantity = quantity,
            status = when {
                status == HoldStatus.HOLDING && !active -> OwnerPickupRequestStatus.EXPIRED
                status == HoldStatus.HOLDING -> OwnerPickupRequestStatus.IN_PROGRESS
                status == HoldStatus.COMPLETED -> OwnerPickupRequestStatus.COMPLETED
                status == HoldStatus.CANCELED_BY_OWNER -> OwnerPickupRequestStatus.UNAVAILABLE
                status == HoldStatus.CANCELED_BY_USER -> OwnerPickupRequestStatus.CANCELLED
                else -> OwnerPickupRequestStatus.EXPIRED
            },
            requestTimeMillis = (heldAt - offset).takeIf { active }, endTimeMillis = (expiresAt - offset).takeIf { active }, isNew = false,
        ),
        canComplete = active && !busy,
        needsCancellation = false,
    )
}
