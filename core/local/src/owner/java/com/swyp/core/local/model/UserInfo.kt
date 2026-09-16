package com.swyp.core.local.model

/**
 * 점주 앱에서 사용하는 매장 정보입니다.
 *
 * [id], [name], [phone]은 점주 개인 정보가 아닌 매장 정보입니다.
 */
data class UserInfo(
    val id: Long,
    val name: String,
    val phone: String,
    val postalCode: String?,
    val address: String,
    val addressDetail: String?,
    val businessOpenTime: String,
    val businessCloseTime: String,
    val businessDays: List<BusinessDay>,
) {
    override fun toString(): String = "UserInfo(REDACTED)"
}
