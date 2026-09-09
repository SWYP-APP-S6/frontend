package com.swyp.mangro.feature.owner.onboarding

/** IDs are provided by the caller's category catalog, not inferred from display names. */
data class StoreCategory(val id: String, val label: String)

data class StoreAddress(val postalCode: String, val address: String)

/** Times are minutes since midnight. Overnight/24-hour hours await a product contract. */
data class StoreRegistration(
    val name: String,
    val category: StoreCategory?,
    val address: StoreAddress?,
    val detailAddress: String,
    val phone: String,
    val openingMinutes: Int?,
    val closingMinutes: Int?,
    val businessDays: Set<Int>,
) {
    val isBasicInfoValid: Boolean
        get() = name.isNotBlank() &&
            category != null &&
            address?.let { it.postalCode.matches(Regex("[0-9]{5}")) && it.address.isNotBlank() } == true

    val isPhoneValid: Boolean
        get() = phone.matches(Regex("0[0-9]{8,10}"))

    val isTimeValid: Boolean
        get() = openingMinutes != null &&
            closingMinutes != null &&
            openingMinutes in 0..1439 &&
            closingMinutes in 0..1439 &&
            openingMinutes < closingMinutes

    val isValid: Boolean
        get() = isBasicInfoValid &&
            isPhoneValid &&
            isTimeValid &&
            businessDays.isNotEmpty() &&
            businessDays.all { it in 0..6 }
}
