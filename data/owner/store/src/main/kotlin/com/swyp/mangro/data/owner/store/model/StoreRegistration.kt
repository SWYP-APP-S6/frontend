package com.swyp.mangro.data.owner.store.model

data class StoreRegistration(
    val name: String,
    val categoryId: String,
    val postalCode: String,
    val address: String,
    val addressDetail: String,
    val phone: String,
    val openingMinutes: Int,
    val closingMinutes: Int,
    /** Sunday = 0, Saturday = 6. */
    val businessDays: Set<Int>,
)
