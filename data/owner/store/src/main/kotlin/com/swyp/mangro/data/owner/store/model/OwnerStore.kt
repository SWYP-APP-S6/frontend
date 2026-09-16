package com.swyp.mangro.data.owner.store.model

enum class StoreApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    UNKNOWN,
    ;

    companion object {
        fun from(value: String): StoreApprovalStatus = entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}

data class OwnerStore(
    val id: Long,
    val name: String,
    val categories: List<String>,
    val status: StoreApprovalStatus,
    val businessOpenTime: String,
    val businessCloseTime: String,
) {
    val canRegisterProduct: Boolean get() = status == StoreApprovalStatus.APPROVED
}
