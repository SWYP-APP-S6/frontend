package com.swyp.mangro.feature.owner.product.model

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestItem
import com.swyp.mangro.core.designsystem.component.card.owner.OwnerPickupRequestStatus
import com.swyp.mangro.feature.owner.product.data.PickupSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun pickupDate(millis: Long, pattern: String = "MM.dd(E) H시 mm분"): String = SimpleDateFormat(pattern, Locale.KOREA).format(Date(millis))

internal fun PickupSnapshot.presentation(now: Long): List<OwnerPickupModel> {
    val targets = pickupShortages(pickups, stock, now).flatMap { it.targets }.map { it.id }.toSet()
    return filterPickups(pickups, PickupFilter.ALL, now).map { pickup ->
        val status = pickup.statusAt(now)
        OwnerPickupModel(
            productId = pickup.productId,
            request = OwnerPickupRequestItem(
                id = pickup.id,
                requestedAt = pickupDate(pickup.requestedAt),
                consumerName = pickup.customerName,
                pickupDeadlineText = "${pickupDate(pickup.deadline, "H시 mm분")}까지 픽업 예정",
                productName = pickup.productName,
                quantity = pickup.quantity,
                status = when (status) {
                    PickupStatus.WAITING -> OwnerPickupRequestStatus.IN_PROGRESS
                    PickupStatus.COMPLETED -> OwnerPickupRequestStatus.COMPLETED
                    PickupStatus.UNAVAILABLE -> OwnerPickupRequestStatus.UNAVAILABLE
                    PickupStatus.CANCELED -> OwnerPickupRequestStatus.CANCELLED
                    PickupStatus.EXPIRED -> OwnerPickupRequestStatus.EXPIRED
                },
                requestTimeMillis = pickup.requestedAt.takeIf { status == PickupStatus.WAITING },
                endTimeMillis = pickup.deadline.takeIf { status == PickupStatus.WAITING },
                isNew = pickup.isNew && status == PickupStatus.WAITING,
            ),
            canComplete = isDemo && canCompletePickup(pickup, pickups, stock, now),
            needsCancellation = pickup.id in targets,
        )
    }
}
