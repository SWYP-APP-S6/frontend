package com.swyp.mangro.feature.owner.home.mapper

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.data.owner.home.model.OwnerHomeProduct
import com.swyp.mangro.data.owner.home.model.OwnerHomeVisit
import com.swyp.mangro.feature.owner.home.screen.model.OwnerHomeVisitor
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

internal fun List<OwnerHomeVisit>.toOwnerHomeVisitor(): PersistentList<OwnerHomeVisitor> = this.map { data ->
    OwnerHomeVisitor(
        id = data.holdId.toString(),
        customerName = data.nickname,
        productName = data.summary,
        quantity = data.totalQty,
        pickupDeadlineMillis = data.expiresAtMillis,
    )
}.toPersistentList()

internal fun List<OwnerHomeProduct>.toOwnerProducts(): PersistentList<OwnerProduct> = this.map { data ->
    OwnerProduct(
        id = data.id.toString(),
        imageUrl = data.photoUrl,
        name = data.name,
        price = data.salePrice,
        remainingCount = data.availableQty,
        expectedVisitCount = data.activeHoldQty,
        shortfallQty = data.shortfallQty,
    )
}.toPersistentList()

internal fun List<String>.toCategories(): String = this.joinToString(" · ") { categoryLabel(it) }

internal fun categoryLabel(category: String): String = when (category) {
    "VEGETABLE" -> "채소"
    "FRUIT" -> "과일"
    "MEAT" -> "육류"
    "SEAFOOD" -> "수산물"
    "DAIRY_EGG" -> "유제품/달걀"
    "BAKERY" -> "베이커리"
    "PREPARED_FOOD" -> "조리식품"
    else -> "기타"
}
