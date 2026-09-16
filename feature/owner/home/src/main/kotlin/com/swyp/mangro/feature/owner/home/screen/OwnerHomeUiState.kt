package com.swyp.mangro.feature.owner.home.screen

import com.swyp.mangro.core.designsystem.component.card.owner.OwnerProduct
import com.swyp.mangro.data.owner.store.model.StoreApprovalStatus
import com.swyp.mangro.data.user.model.UserProfile
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

data class OwnerHomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: Int? = null,
    val profile: UserProfile? = null,
    val approvalStatus: StoreApprovalStatus = StoreApprovalStatus.UNKNOWN,
    val canRegisterProduct: Boolean = false,
    val attentionAvailable: Boolean = false,
    val unreadNotificationCount: Long = 0,
    val completingPickupIds: PersistentSet<String> = persistentSetOf(),
    val hasRegisteredProduct: Boolean = false,
    val storeName: String = "",
    val storeCategory: String = "",
    val expectedVisitCount: Int = 0,
    val completedPickupCount: Long = 0,
    val sellingCount: Int = 0,
    val hasNewPickup: Boolean = false,
    val cancellationRequiredCount: Int = 0,
    val needsPickupConfirmation: Boolean = false,
    val isAttentionDismissed: Boolean = false,
    val visitors: PersistentList<OwnerHomeVisitor> = persistentListOf(),
    val products: PersistentList<OwnerProduct> = persistentListOf(),
) {
    val hasAttention: Boolean
        get() = cancellationRequiredCount > 0 || needsPickupConfirmation

    val showAttention: Boolean
        get() = hasAttention && !isAttentionDismissed
}

data class OwnerHomeVisitor(
    val id: String,
    val customerName: String,
    val productName: String,
    val quantity: Int,
    val pickupDeadlineMillis: Long,
)
